package degallant.github.io.todoapp;

import degallant.github.io.todoapp.authentication.ApiKeyEntity;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
public class ApiKeyDetailsService implements UserDetailsService {

    private final ApiKeyRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID uuid;
        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException(username);
        }

        var apiKey = repository.findByIdAndDeletedAtIsNull(uuid);

        if (apiKey.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }

        return new ApiKeyDetails(apiKey.get());
    }

    @RequiredArgsConstructor
    public static class ApiKeyDetails implements UserDetails {

        private final ApiKeyEntity apiKeyEntity;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public String getPassword() {
            return apiKeyEntity.getSecret();
        }

        @Override
        public String getUsername() {
            return apiKeyEntity.getId().toString();
        }

        @Override
        public boolean isAccountNonExpired() {
            return !apiKeyEntity.isDeleted();
        }

        @Override
        public boolean isAccountNonLocked() {
            return !apiKeyEntity.isDeleted();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return !apiKeyEntity.isDeleted();
        }

        @Override
        public boolean isEnabled() {
            return !apiKeyEntity.isDeleted();
        }

    }

}
