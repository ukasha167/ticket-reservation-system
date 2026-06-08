import static org.junit.Assert.*;
import org.junit.Test;

public class ValidationUtilsTest {

	@Test
    public void testValidName() {
        assertTrue(ValidationUtils.isValidName("John Doe"));
        assertFalse(ValidationUtils.isValidName(""));
        assertFalse(ValidationUtils.isValidName("   "));
        assertFalse(ValidationUtils.isValidName(null));
    }

    @Test
    public void testValidCNIC() {
        assertTrue(ValidationUtils.isValidCNIC("1234567890123"));
        assertFalse(ValidationUtils.isValidCNIC("123456789012"));
        assertFalse(ValidationUtils.isValidCNIC("12345678901234"));
        assertFalse(ValidationUtils.isValidCNIC("12345abc90123"));
    }

    @Test
    public void testValidPhone() {
        assertTrue(ValidationUtils.isValidPhone("03001234567"));
        assertFalse(ValidationUtils.isValidPhone("0300123456"));
        assertFalse(ValidationUtils.isValidPhone("030012345678"));
        assertFalse(ValidationUtils.isValidPhone("Phone Number"));
    }

}
