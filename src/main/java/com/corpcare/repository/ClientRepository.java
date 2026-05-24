package com.corpcare.repository;

import com.corpcare.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByContactEmail(String contactEmail);
}
