package io.eventuate.tram.sagas.participant;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SagaLockManagerImpl implements SagaLockManager {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public boolean claimLock(String sagaType, String sagaId, String target) {
    while (true)
      try {
        jdbcTemplate.update("INSERT INTO saga_lock_table(target, saga_type, saga_id) VALUES(?, ?,?)", target, sagaType, sagaId);
        logger.debug("Saga {} {} has locked {}", sagaType, sagaId, target);
        return true;
      } catch (DuplicateKeyException e) {
        Optional<String> owningSagaId = selectForUpdate(target);
        if (owningSagaId.isPresent()) {
          if (owningSagaId.get().equals(sagaId))
            return true;
          else {
            logger.debug("Saga {} {} is blocked by {} which has locked {}", sagaType, sagaId, owningSagaId, target);
            return false;
          }
        }
        logger.debug("{}  is repeating attempt to lock {}", sagaId, target);
      }
  }

  private Optional<String> selectForUpdate(String target) {
    return Optional.ofNullable(DataAccessUtils.singleResult(jdbcTemplate.query("select saga_id from saga_lock_table WHERE target = ? FOR UPDATE", (rs, rowNum) -> {
            return rs.getString("saga_id");
          }, target)));
  }

  @Override
  public void stashMessage(String sagaType, String sagaId, String target, Message message) {

    logger.debug("Stashing message from {} for {} : {}", sagaId, target, message);

    jdbcTemplate.update("INSERT INTO saga_stash_table(message_id, target, saga_type, saga_id, message_headers, message_payload) VALUES(?, ?,?, ?, ?, ?)",
            message.getRequiredHeader(Message.ID),
            target,
            sagaType,
            sagaId,
            JSonMapper.toJson(message.getHeaders()),
            message.getPayload()
            );
  }

  @Override
  public Optional<Message> unlock(String sagaId, String target) {
    Optional<String> owningSagaId = selectForUpdate(target);
    Assert.isTrue(owningSagaId.isPresent());
    Assert.isTrue(owningSagaId.get().equals(sagaId), String.format("Expected owner to be %s but is %s", sagaId, owningSagaId.get()));

    logger.debug("Saga {} has unlocked {}", sagaId, target);

    List<StashedMessage> stashedMessages = jdbcTemplate.query("select message_id, target, saga_type, saga_id, message_headers, message_payload from saga_stash_table WHERE target = ? ORDER BY message_id LIMIT 1", (rs, rowNum) -> {
      return new StashedMessage(rs.getString("saga_type"), rs.getString("saga_id"),
              MessageBuilder.withPayload(rs.getString("message_payload")).withExtraHeaders("",
                      JSonMapper.fromJson(rs.getString("message_headers"), Map.class)).build());
    }, target);

    if (stashedMessages.isEmpty()) {
      assertEqualToOne(jdbcTemplate.update("delete from saga_lock_table where target = ?", target));
      return Optional.empty();
    }

    StashedMessage stashedMessage = stashedMessages.get(0);

    logger.debug("unstashed from {}  for {} : {}", sagaId, target, stashedMessage.getMessage());

    assertEqualToOne(jdbcTemplate.update("update saga_lock_table set saga_type = ?, saga_id = ? where target = ?", stashedMessage.getSagaType(),
            stashedMessage.getSagaId(), target));
    assertEqualToOne(jdbcTemplate.update("delete from saga_stash_table where message_id = ?", stashedMessage.getMessage().getId()));

    return Optional.of(stashedMessage.getMessage());
  }

  private void assertEqualToOne(int n) {
    if (n != 1)
      throw new RuntimeException("Expected to update one row but updated: " + n);
  }
}
