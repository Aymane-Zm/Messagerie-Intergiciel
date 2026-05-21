package com.example.database.service;

import com.example.database.model.ClientConnecte;
import com.example.database.model.Message;
import com.example.database.repo.ClientRepository;
import com.example.database.repo.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KafkaService {

    private final ClientRepository repo;
    private final MessageRepository messageRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${application.topictechin}")
    private String topicTechIn;

    public KafkaService(ClientRepository repo, KafkaTemplate<String, String> kafkaTemplate, MessageRepository messageRepo) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
        this.messageRepo = messageRepo;
    }

    @KafkaListener(topics = "topictechout", groupId = "client-cons-db")
    public void listen(String message) {
        System.out.println(" [client-cons-db] Message reçu : " + message);

        if (message.startsWith("CONNECT:")) {
            String client = message.split(":")[1];
            repo.save(new ClientConnecte(client, true));
            System.out.println(" Client connecté : " + client);

        } else if (message.startsWith("DISCONNECT:")) {
            String client = message.split(":")[1];
            repo.deleteById(client);
            System.out.println(" Client déconnecté : " + client);

        } else if (message.startsWith("GET:")) {
            String demandeur = message.split(":")[1];
            List<ClientConnecte> clients = repo.findAll();

            StringBuilder sb = new StringBuilder();
            sb.append("FROM:client-cons-db#TO:")
              .append(demandeur)
              .append("#\"");

            for (ClientConnecte c : clients) {
                sb.append(c.getNom()).append(", ");
            }

            String msgFinal = sb.toString().replaceAll(", $", "\"");
            kafkaTemplate.send(topicTechIn, msgFinal);

            System.out.println(" Réponse envoyée à " + demandeur + " : " + msgFinal);

        } else if (message.startsWith("ISCONNECTED:")) {
            try {
                String[] parts = message.split(":")[1].split("#");
                String demandeur = parts[0];
                String cible = parts[1];

                boolean exists = repo.existsById(cible);

                String reponse = "FROM:client-cons-db#TO:" + demandeur + "#\"" +
                        (exists ? " " + cible + " est connecté." : " " + cible + " n'est pas connecté.") + "\"";

                kafkaTemplate.send(topicTechIn, reponse);

                System.out.println(" Réponse à ISCONNECTED → " + reponse);
            } catch (Exception e) {
                System.err.println(" Erreur ISCONNECTED: " + e.getMessage());
            }

        } else {
            System.out.println(" Message non reconnu : " + message);
        }
    }

    @KafkaListener(topics = "topicin", groupId = "client-cons-db")
    public void archiverMessage(String message) {
        System.out.println("🗂 Message à archiver reçu : " + message);

        try {
            String[] parts = message.split("#");
            if (parts.length != 3) return;

            String from = parts[0].replace("FROM:", "");
            String to = parts[1].replace("TO:", "");
            String contenu = parts[2].replaceAll("^\"|\"$", "");

            Message m = new Message(from, to, contenu, LocalDateTime.now());
            messageRepo.save(m);

            System.out.println(" Message archivé : " + from + " → " + to + " : " + contenu);
        } catch (Exception e) {
            System.err.println(" Erreur archivage message : " + e.getMessage());
        }
    }
}
