# EMSI — Activité Pratique N°1 – Implémentation d'un microservice

Service de comptes bancaires (Account Service) réalisé dans le cadre d’une activité pratique EMSI, inspirée du parcours pédagogique Spring / microservices (référence Youssfi).

> Intitulé de travail : « Implémentation d'un microservice ». Les formulations exactes du support Classroom non consultables ici ne sont pas affirmées comme titre officiel.

**Dépôt GitHub cible :** `emsi-tp01-microservice`

---

## Description technique

Microservice Spring Boot exposant la gestion de comptes bancaires via :

- API REST personnalisée
- Spring Data REST
- GraphQL (schema + GraphiQL)
- Documentation OpenAPI / Swagger UI

Persistance en mémoire avec H2.

## Objectif pédagogique

- Découvrir Spring Boot comme socle d’un microservice
- Modéliser une entité métier (`BankAccount`) et ses DTO
- Exposer plusieurs styles d’API (REST, Data REST, GraphQL)
- Introduire tests unitaires et d’intégration Maven

## Technologies

| Composant | Version / choix |
|-----------|-----------------|
| Java | 17 |
| Spring Boot | 2.7.3 |
| Spring Web | oui |
| Spring Data JPA | oui |
| Spring Data REST | base-path `/data-rest` |
| Spring GraphQL | GraphiQL activé |
| Springdoc OpenAPI | 1.6.14 |
| H2 | mémoire (`account-db`) |
| Lombok | oui |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

Vérifié dans `pom.xml` et `src/main/resources/application.properties`.

## Fonctionnalités

- CRUD comptes : `GET/POST/PUT/DELETE /bankAccounts`
- Accès Data REST sous `/data-rest`
- API GraphQL (queries / mutations selon `schema.graphqls`)
- Console H2 et GraphiQL activées en local
- Collection Postman dans `postman/`
- Suite de tests Maven (37 méthodes `@Test` recensées dans `src/test`)

## Architecture

```text
Client HTTP / GraphiQL / Swagger
        │
        ▼
Spring Boot :8081
  ├── BankAccountRestController
  ├── Spring Data REST
  ├── GraphQL Controller
  └── JPA → H2 (mem:account-db)
```

## Structure des dossiers

```text
springboot/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/
├── .gitignore
├── postman/                 # collection et résultats HTTP locaux
├── src/main/java/...        # entities, dtos, services, web, mappers
├── src/main/resources/
│   ├── application.properties
│   └── graphql/schema.graphqls
└── src/test/java/...
```

## Prérequis

- JDK 17+
- Maven 3.8+ **ou** Maven Wrapper fourni

## Installation

```powershell
cd springboot
.\mvnw.cmd -v
```

## Démarrage

```powershell
cd springboot
.\mvnw.cmd spring-boot:run
```

Service : `http://localhost:8081`

| Ressource | URL |
|-----------|-----|
| REST | `http://localhost:8081/bankAccounts` |
| Data REST | `http://localhost:8081/data-rest` |
| GraphiQL | `http://localhost:8081/graphiql` |
| OpenAPI | `http://localhost:8081/v3/api-docs` (selon springdoc) |
| H2 console | `http://localhost:8081/h2-console` |

## Exemples d’utilisation

```http
GET  http://localhost:8081/bankAccounts
GET  http://localhost:8081/bankAccounts/{id}
POST http://localhost:8081/bankAccounts
Content-Type: application/json

{
  "balance": 1000,
  "type": "CURRENT_ACCOUNT",
  "currency": "MAD"
}
```

Importer aussi `postman/TP01-Bank-Account-REST-API.postman_collection.json`.

## Tests

```powershell
.\mvnw.cmd test
```

Couverture locale : DAO, mapper, service, REST, GraphQL, OpenAPI, Data REST (37 `@Test`).

## Limites connues

- H2 en mémoire : données perdues au redémarrage
- Pas d’authentification / autorisation
- Pas d’Eureka, Gateway ni Config Server (hors périmètre de ce TP)
- Logs de session (`*.log`) destinés à rester locaux (voir `.gitignore`)

## Références pédagogiques

- Cours / atelier EMSI — microservices Spring
- Support pédagogique associé (parcours Youssfi — Account Service)
- Documentation Spring Boot 2.7, Spring Data REST, Spring GraphQL, Springdoc
