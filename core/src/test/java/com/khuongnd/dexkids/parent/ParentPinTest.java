package com.khuongnd.dexkids.parent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ParentPinTest {
    @Test void saltedCredentialsAuthenticateOnlyCorrectPin() {
        String first = ParentPin.encode("012345");
        assertNotEquals(first, ParentPin.encode("012345"));
        assertTrue(ParentPin.verify("012345", first));
        assertFalse(ParentPin.verify("012346", first));
        assertFalse(ParentPin.verify("12345", first));
    }
    @Test void malformedCredentialsAndPinsFailClosed() {
        for (String pin : new String[] {"123", "123456789", "12ab", "１２３４", "1234\n", ""}) {
            assertFalse(ParentPin.valid(pin));
            assertThrows(IllegalArgumentException.class, () -> ParentPin.encode(pin));
        }
        for (String stored : new String[] {"", ":", "a:b:c", "invalid", "!!!!:!!!!", "YQ==:Yg=="})
            assertFalse(ParentPin.verify("1234", stored));
    }
}
