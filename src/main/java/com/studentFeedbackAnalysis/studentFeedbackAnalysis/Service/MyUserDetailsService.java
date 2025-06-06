package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.User;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.UserPrincipal;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            System.out.println("user not found");
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new UserPrincipal(user);
    }
}
