public class ValidationUtils {

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isValidCNIC(String cnic) {
        if (cnic == null) return false;
        return cnic.matches("\\d{13}");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{11}");
    }
}
