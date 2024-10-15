<br/>
<p align="center">
    <a href="https://www.witboost.com/">
        <img src="docs/img/witboost_logo.svg" alt="witboost" width=600 >
    </a>
</p>
<br/>

Designed by [Agile Lab](https://www.agilelab.it/), Witboost is a versatile platform that addresses a wide range of sophisticated data engineering challenges. It enables businesses to discover, enhance, and productize their data, fostering the creation of automated data platforms that adhere to the highest standards of data governance. Want to know more about Witboost? Check it out [here](https://www.witboost.com/) or [contact us!](https://witboost.com/contact-us)

This repository is part of our [Starter Kit](https://github.com/agile-lab-dev/witboost-starter-kit) meant to showcase Witboost's integration capabilities and provide a "batteries-included" product.

# CDP S3 Specific Provisioner

- [Overview](#overview)
- [Building](#building)
- [Running](#running)
- [Configuring](#configuring)
- [Deploying](#deploying)
- [HLD](docs/HLD.md)
- [API specification](docs/API.md)

## Overview

This project implements a simple Specific Provisioner that provision S3 Storage for a CDP environment.

### What's a Specific Provisioner?

A Specific Provisioner is a microservice which is in charge of deploying components that use a specific technology. When the deployment of a Data Product is triggered, the platform generates it descriptor and orchestrates the deployment of every component contained in the Data Product. For every such component the platform knows which Specific Provisioner is responsible for its deployment, and can thus send a provisioning request with the descriptor to it so that the Specific Provisioner can perform whatever operation is required to fulfill this request and report back the outcome to the platform.

You can learn more about how the Specific Provisioners fit in the broader picture [here](https://docs.witboost.agilelab.it/docs/p2_arch/p1_intro/#deploy-flow).

### CDP

Cloudera Data Platform (CDP) is an integrated data management and analytics platform that empowers organizations to efficiently collect, store, and analyze data from various sources. CDP offers a unified environment for data management, enabling businesses to make informed decisions, gain insights, and drive data-driven innovation. Explore CDP further with the official documentation: [Cloudera Data Platform Documentation](https://www.cloudera.com/products/cloudera-data-platform.html).

### Software stack

This microservice is written in Scala 2.13, using HTTP4S for the HTTP layer. Project is built with SBT and supports packaging as JAR, fat-JAR and Docker image, ideal for Kubernetes deployments (which is the preferred option).

## Building

**Requirements:**

- Java 11
- SBT
- Docker (for building images only)

This project also depends on Witboost library [scala-mesh-commons](https://github.com/agile-lab-dev/witboost-scala-mesh-commons), published Open-Source on Maven Central.

**Generating sources:** this project uses OpenAPI as standard API specification and the [sbt-guardrail](https://github.com/guardrail-dev/sbt-guardrail) plugin to generate server code from the [specification](./api/src/main/openapi/interface-specification.yml).

The code generation is done automatically in the compile phase:

```bash
sbt compile
```

**Tests:** are handled by the standard task as well:

```bash
sbt test
```

**Artifacts & Docker image:** the project uses SBT Native Packager for packaging. Build artifacts with:

```
sbt package
```

To build an image using the local Docker server:

```
sbt docker:publishLocal
```

*Note:* the version for the project is automatically computed using the environment variable `VERSION`.

**CI/CD:** the pipeline is based on GitLab CI as that's what we use internally. It's configured by the `.gitlab-ci.yaml` file in the root of the repository. You can use that as a starting point for your customizations.

## Running

To run the server locally, use:

```bash
sbt compile run
```

By default, the server binds to port 8093 on localhost. After it's up and running you can make provisioning requests to this address.

## Configuring

Most application configurations are handled with the Typesafe Config library. You can find the default settings in the `reference.conf`. Customize them and use the `config.file` system property or the other options provided by Typesafe Config according to your needs.

Logging is handled with Logback. Customize it and pass it using the `logback.configurationFile` system property.

This Specific Provisioner uses the followings SDK:

- **CDP SDK**: please refer to the [official documentation](https://docs.cloudera.com/cdp-public-cloud/cloud/sdk/topics/mc-overview-of-the-cdp-sdk-for-java.html) to setup the access credentials
- **AWS SDK**: please refer to the [official documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup-basics.html) to setup the access credentials

### Environment variables

This Specific Provisioner expects the following environment variables to be defined:

| Variable                 | Description                                               |
|:-------------------------|:----------------------------------------------------------|
| CDP_DEPLOY_ROLE_USER     | The CDP user to be used with CDP services (eg Ranger)     |
| CDP_DEPLOY_ROLE_PASSWORD | The CDP password to be used with CDP services (eg Ranger) |

The CDP user must be a `Machine User` and needs to have at least the following roles:
- EnvironmentAdmin
- EnvironmentUser

### Provisioner configuration

| Configuration                               | Description                                          | Default   |
|:--------------------------------------------|:-----------------------------------------------------|:----------|
| provisioner.networking.httpServer.interface | Interface to bind the specific provisioner API layer | `0.0.0.0` |
| provisioner.networking.httpServer.port      | Port to bind the specific provisioner API layer      | `8093`    |

Example:

```
provisioner {
  networking {
    httpServer {
      port: 8093
      interface: "0.0.0.0"
    }
  }
}
```

### Service configuration

| Configuration                          | Description                                                                | Default |
|:---------------------------------------|:---------------------------------------------------------------------------|:--------|
| s3.principalsMappingPlugin             | Object containing the configuration for instantiating the PrincipalsMapper |         |  
| s3.principalsMappingPlugin.pluginClass | Fully qualified name of the PrincipalsMapperFactory to be instantiated     |         |

Each PrincipalsMapperFactory may require additional configuration information to create the PrincipalsMapper instance. This configuration is retrieved from an object in the `principalsMappingPlugin` config object. The key of this object shall be equal to the `configIdentifier` of the instantiated PrincipalsMapperFactory

Example of configuration to load `FreeIpaIdentityPrincipalsMapper`:

```
s3 {
  principalsMappingPlugin {
    pluginClass = "it.agilelab.provisioning.commons.principalsmapping.impl.freeipa.FreeIpaIdentityPrincipalsMapperFactory"
    freeipa-identity {}
  }
}
```

## Deploying

This microservice is meant to be deployed to a Kubernetes cluster. We provide an [helm chart](./helm) for this purpose.

## License

This project is available under the [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0); see [LICENSE](LICENSE) for full details.

## About us

<br/>
<p align="center">
    <a href="https://www.agilelab.it">
        <img src="docs/img/agilelab_logo.svg" alt="Agile Lab" width=600>
    </a>
</p>
<br/>

Agile Lab creates value for its Clients in data-intensive environments through customizable solutions to establish performance driven processes, sustainable architectures, and automated platforms driven by data governance best practices.

Since 2014 we have implemented 100+ successful Elite Data Engineering initiatives and used that experience to create Witboost: a technology agnostic, modular platform, that empowers modern enterprises to discover, elevate and productize their data both in traditional environments and on fully compliant Data mesh architectures.

[Contact us](https://www.agilelab.it/contacts) or follow us on:
- [LinkedIn](https://www.linkedin.com/company/agile-lab/)
- [Instagram](https://www.instagram.com/agilelab_official/)
- [YouTube](https://www.youtube.com/channel/UCTWdhr7_4JmZIpZFhMdLzAA)
- [Twitter](https://twitter.com/agile__lab)
