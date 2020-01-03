# Custom Clever Cloud client generator

Download the latest codegen project and the latest API definition.

```
wget http://central.maven.org/maven2/org/openapitools/openapi-generator-cli/4.2.0/openapi-generator-cli-4.2.0.jar
wget https://api.clever-cloud.com/v2/openapi.json
```

## Common configuraiton

All clients require Oauth1 tokens for authentication. They will all look for the following environment variables:

```
export CC_ACCESS_TOKEN=********
export CC_ACCESS_SECRET=*******
export CC_CONSUMER_KEY==*******
export CC_CONSUMER_SECRET==*******
```

To generate your own, please follow this guide: [https://github.com/CleverCloud/oauth-consumer-server](https://github.com/CleverCloud/oauth-consumer-server)

## Clients

### Go client

For the go client templates to include oauth1 auth in ./go. Copy has been made at version 4.2.0.

Clean generation output: `rm -rf ./golang/go-test/clevercloud`

Run the generation: `java -jar openapi-generator-cli-4.2.0.jar generate -c config-go.json -i openapi.json -g go -o ./golang/go-test/clevercloud -t golang/go --skip-validate-spec`

Execute the test: `cd golang/go-test && go run clever-request.go`

### Spring Webflux Client

Run the generation: `java -jar openapi-generator-cli-4.2.0.jar generate -c config-java.json -i openapi.json -g java -o ./spring/clevercloud-java-client  --library webclient --skip-validate-spec`

Test project in `spring/spring-flux-test`

### Javascript Client

Run the generation: `java -jar openapi-generator-cli-4.2.0.jar generate -i openapi.json -g javascript -o ./javascript/js --skip-validate-spec`

### Elixir Client

Run the generation: `java -jar openapi-generator-cli-4.2.0.jar generate -i openapi.json -g elixir -o ./elixir/clevercloud-elixir-client --skip-validate-spec`
