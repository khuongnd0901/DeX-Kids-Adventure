package com.khuongnd.dexkids.parent;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local parental gate credential; no raw PIN persistence. */
public final class ParentPin {
    private ParentPin() { }
    public static boolean valid(String pin) { return pin != null && pin.matches("[0-9]{4,8}"); }
    public static String encode(String pin) {
        if (!valid(pin)) throw new IllegalArgumentException("Use 4 to 8 digits");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(derive(pin, salt));
    }
    public static boolean verify(String pin, String stored) {
        if (!valid(pin) || stored == null) return false;
        try {
            String[] parts = stored.split(":", -1);
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            return salt.length == 16 && expected.length == 32
                    && MessageDigest.isEqual(expected, derive(pin, salt));
        } catch (IllegalArgumentException ex) { return false; }
    }
    private static byte[] derive(String pin, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, 120_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("PIN key derivation unavailable", ex);
        } finally { spec.clearPassword(); }
    }
}
