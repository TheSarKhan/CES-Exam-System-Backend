package scratch;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$WqB./5pS18f9M8wQY8u8k.o4E91tYh2.qB.pI3u/B.s.J.U/14q2m";
        String pass = "admin123";
        System.out.println("Matches? " + encoder.matches(pass, hash));
        System.out.println("New Hash: " + encoder.encode(pass));
    }
}
