package com.example.database.repo;

import com.example.database.model.ClientConnecte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientConnecte, String> {}
