
package com.ticketflow.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ResetOrganizerPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Temporary password for local project testing
        System.out.println(encoder.encode("Organizer@123"));
    }
}
