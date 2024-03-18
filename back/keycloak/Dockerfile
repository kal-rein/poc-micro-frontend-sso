FROM quay.io/keycloak/keycloak:24.0.1

VOLUME /opt/keycloak/data/import

ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KC_PROXY=edge

EXPOSE 80

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--http-port=80", "--log-level=DEBUG"]
