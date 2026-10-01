# bella

This project uses Quarkus, the Supersonic Subatomic Java Framework, and requires **Java 25**.

Production runs on a single EC2 instance in São Paulo (`sa-east-1`), provisioned with
Terraform (`infra/terraform`). Every push to `main` is deployed automatically by a
self-hosted GitHub Actions runner on that instance — see [docs/aws.md](docs/aws.md).

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

### Frontend (Vue) + Orion Users (auth) in dev

The web UI (`frontend/`) authenticates against Orion Users (login/registration/2FA),
an external service that is **not** part of this repo and must be running for
login/signup to work — see `docker-compose.yml` (service `orion-users`, built from
[orion-services/users](https://github.com/orion-services/users)).

```shell script
# 1. Start Orion Users (+ its own Postgres/Redis, independent from Quarkus Dev
#    Services used by `quarkus:dev`). Requires a .env — see .env.example.
cp .env.example .env    # fill in POSTGRES_PASSWORD and Gmail SMTP credentials;
                        # use the local ORION_USERS_EMAIL_VALIDATION_URL shown there
docker compose up -d postgres redis orion-users
# Orion Users is now reachable at http://localhost:8082 (ORION_USERS_HOST_PORT)

# 2. Create frontend/.env once (see the warning below for why this matters)
cd frontend && cp .env.example .env && cd ..   # VITE_ORION_USERS_URL already defaults to http://localhost:8082

# 3. Run the Quarkus backend as usual — no separate frontend server needed.
./mvnw quarkus:dev
```

`./mvnw quarkus:dev` already builds the Vue app (`frontend-maven-plugin` runs
`npm install` + `npm run build` on startup) and embeds it into
`src/main/resources/META-INF/resources/`, served by Quarkus itself on
<http://localhost:8080>. **There is no need to run a separate `npm run dev` /
Vite dev server** for normal usage.

The tradeoff: that build only happens once, when `quarkus:dev` starts. If you
edit anything under `frontend/src/**` while `quarkus:dev` is already running,
restart it (`Ctrl+C` then `./mvnw quarkus:dev` again) to rebuild and pick up
the change — Quarkus's live-reload only watches `src/main/java` and
`src/main/resources`, not `frontend/src`.

If you *do* want instant hot-reload while actively developing Vue components,
you can optionally run a separate Vite dev server instead (`cd frontend && npm
run dev`, served at `http://localhost:5173`, proxying API calls to `:8080`) —
but that's a pure convenience for frontend-only iteration, not required.

If `frontend/.env` is missing, `VITE_ORION_USERS_URL` falls back to
`http://localhost:8082` (the Orion Users port published by Docker Compose).
Bella itself listens on `8080` and does not implement `/users/*`. Always create
`frontend/.env` from `frontend/.env.example` before testing the web UI locally.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/bella-1.0.0-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
