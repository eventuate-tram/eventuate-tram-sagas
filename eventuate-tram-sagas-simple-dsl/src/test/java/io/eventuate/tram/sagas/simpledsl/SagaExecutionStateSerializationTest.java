package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 * Serialization/Deserialization test for {@link SagaExecutionState}.
 *
 * @author nbikbaev
 */
public class SagaExecutionStateSerializationTest {

    @Test
    public void shouldSerialize() throws JSONException {
        //given:
        final SagaExecutionState sagaExecutionState = new SagaExecutionState(-1, false, false);

        //when:
        final String actualJson = JSonMapper.toJson(sagaExecutionState);
        final String expectedJson = "{\"currentlyExecuting\":-1,\"compensating\":false,\"endState\":false}";

        //then:
        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void shouldDeserialize() throws JSONException {
        //given:
        final SagaExecutionState expectedState = new SagaExecutionState(-1, false, false);
        final String jsonValue = "{\"currentlyExecuting\":-1,\"compensating\":false,\"endState\":false}";

        //when:
        final SagaExecutionState actualState = JSonMapper.fromJson(jsonValue, SagaExecutionState.class);

        //then:
        ReflectionAssert.assertReflectionEquals(expectedState, actualState);
    }

}