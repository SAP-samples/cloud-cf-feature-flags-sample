[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-feature-flags-sample)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-feature-flags-sample)

# Flagship demo application

Flagship Demo Application is a simple Spring Boot application that consumes [Flagship](https://www.flagship.io/) on SAP Cloud Platform, Cloud Foundry environment.
It implements a [feature toggle](https://en.wikipedia.org/wiki/Feature_toggle) (evaluation call to Flagship) and exposes this feature toggle through a Web user interface.
There is also one REST end-point that reads the value of `VCAP_SERVICES` environment variable.

## Prerequisites

* have set up [Maven 3.0.x](http://maven.apache.org/install.html)
* have an [SAP Cloud Platform trial account on Cloud Foundry environment](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/e50ab7b423f04a8db301d7678946626e.html)
* have a [trial space on a Cloud Foundry instance](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/fa5deb9cc4be4ca58070456cd2c47647.html#loioe9aed07891e545dd88192df013646897)
* have set up a [curl](https://curl.haxx.se/download.html) plug-in for cmd
* have [installed cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html)
* have an account in [Flagship](https://app.flagship.io/)

## Running the application on SAP Cloud Platform

Follow these steps to run the Flagship Demo application on SAP Cloud Platform, Cloud Foundry environment.

> **Note:** This guide uses the Cloud Foundry trial account on Europe (Frankfurt) region (https://account.hanatrial.ondemand.com/cockpit#/home/overview).
> If you want to use a different region, you have to modify the domain in the requests.
> For more information about regions and hosts on SAP Cloud Platform, Cloud Foundry environment, see [Regions and Hosts](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html).

<!-- toc -->

- [1. Build the feature-flags-demo application](#1-build-the-feature-flags-demo-application)
- [2. Edit application name in manifest file](#2-edit-application-name-in-manifest-file)
- [3. Deploy feature-flags-demo on SAP Cloud Platform](#3-deploy-feature-flags-demo-on-sap-cloud-platform)
- [4. Create a user provided service with Flagship connection information](#4-create-a-user-provided-service-with-flagship-connection-information)
    * [4.1 Get Flagship environment id and API key](#41-get-flagship-environment-id-and-api-key)
    * [4.2 Create a user provided service](#42-create-a-user-provided-service)
- [5. Call the feature-flags-demo application's /vcap_services endpoint](#5-call-the-feature-flags-demo-applications-vcap_services-endpoint)
- [6. Bind feature-flags-demo to flagship-instance](#6-bind-feature-flags-demo-to-flagship-instance)
- [7. Restage feature-flags-demo](#7-restage-feature-flags-demo)
- [8. Ensure that flagship-instance is bound to feature-flags-demo](#8-ensure-that-flagship-instance-is-bound-to-feature-flags-demo)
- [Accessing the demo application](#accessing-the-demo-application)
- [Accessing Flagship UI](#accessing-flagship-ui)
- [9. Evaluate a missing Feature Flag](#9-evaluate-a-missing-feature-flag)
- [10. Create a new boolean Feature Flag](#10-create-a-new-boolean-feature-flag)
- [11. Evaluate the newly created boolean Feature Flag](#11-evaluate-the-newly-created-boolean-feature-flag)
- [12. Enable the boolean Feature Flag](#12-enable-the-boolean-feature-flag)
- [13. Verify that the boolean Feature Flag is enabled](#13-verify-that-the-boolean-feature-flag-is-enabled)
- [14. Create a new string Feature Flag](#14-create-a-new-string-feature-flag)
- [15. Evaluate the newly created string Feature Flag](#15-evaluate-the-newly-created-string-feature-flag)
- [16. Enable the string Feature Flag](#16-enable-the-string-feature-flag)
- [17. Verify that the string Feature Flag is enabled](#17-verify-that-the-string-feature-flag-is-enabled)
- [18. Specify direct delivery strategy of a variation of the string flag](#18-specify-direct-delivery-strategy-of-a-variation-of-the-string-flag)
- [19. Evaluate the string Feature Flag](#19-evaluate-the-string-feature-flag)

<!-- tocstop -->

### 1. Build the feature-flags-demo application

```bash
$ git clone git@github.com:SAP/cloud-cf-feature-flags-sample.git
$ cd cloud-cf-feature-flags-sample
$ mvn clean install
```

> **Note:** Alternatively, you can use the Eclipse IDE, use the `clean install` goal from _Run As > Maven Build..._ menu.

### 2. Edit application name in manifest file

Due to CloudFoundry's limitation in regard to application naming it's quite possible for someone to have already deployed the demo application with the **feature-flags-demo** name as it is currently set in the **manifest.yml** file. CloudFoundry will not allow another application with the same name to be deployed, so you **MUST** edit the manifest file and change the application name before deploying. For example:

```yaml
---
applications:
- name: feature-flags-demo123
  path: target/feature-flags-demo.jar
```

> **Note:** Use the modified value in the commands which require application name (e.g. cf bind-service)
and when requesting the application in the browser or via curl.

### 3. Deploy feature-flags-demo on SAP Cloud Platform

```bash
$ cf api https://api.cf.eu10.hana.ondemand.com
$ cf login
$ cf push
```

### 4. Create a user provided service with Flagship connection information

#### 4.1 Get Flagship environment id and API key

1. Login to [Flagship](https://app.flagship.io/) and open _Settings_ -> _Environment Settings_.
2. Copy the corresponding environment id and API key.

#### 4.2 Create a user provided service

Execute the following command using the environment id and API key from the previous step.

```bash
$ cf create-user-provided-service flagship-instance -t flagship-flags-service -p "{ \"base-uri\": \"https://decision.flagship.io\", \"env-id\": \"<env-id>\", \"api-key\": \"<api-key>\" }"

-----
Creating user provided service flagship-instance in org <ORG_ID> / space dev as <USER_ID>...
OK
```

### 5. Call the feature-flags-demo application's /vcap_services endpoint

> **Note**: Expect to receive an empty JSON.

The /vcap_services end-point simply returns the content of  _VCAP_SERVICES_ environment variable. As for now there is no service instances bound to `feature-flags-demo`, so you will receive an empty JSON.

In the command you use the following URL: \<application_URL\>/vcap_services. You can find the \<application_URL\> in the SAP Cloud Platform Cockpit, in the _feature-flag-demo > Overview > Application Routes_.

```bash
$ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services
```

### 6. Bind feature-flags-demo to flagship-instance

```bash
$ cf bind-service feature-flags-demo flagship-instance

-----
Binding service flagship-instance to app feature-flags-demo in org <ORG_ID> / space dev as <USER_ID>...
OK
TIP: Use 'cf restage feature-flags-demo' to ensure your env variable changes take effect
```

### 7. Restage feature-flags-demo

Restage `feature-flags-demo` application so the changes in the application environment take effect.

```bash
$ cf restage feature-flags-demo
```

### 8. Ensure that flagship-instance is bound to feature-flags-demo

> **Note**: Expect to receive the injected environment variables.

```bash
$ curl https://feature-flags-demo.cfapps.sap.hana.ondemand.com/vcap_services
```

Sample JSON response:
```json
{
  "user-provided": [
    {
      "label": "user-provided",
      "name": "flagship-instance",
      "tags": [
        "flagship-flags-service"
      ],
      "instance_guid": "...",
      "instance_name": "flagship-instance",
      "binding_guid": "...",
      "binding_name": null,
      "credentials": {
        "api-key": "...",
        "base-uri": "https://decision.flagship.io",
        "env-id": "..."
      },
      "syslog_drain_url": "",
      "volume_mounts": []
    }
  ]
}
```

### Accessing the demo application

The web interface of the demo application will be accessed multiple times throughout this tutorial.
Here is how to open it: navigate to feature-flags-demo application overview in the SAP Cloud Platform Cockpit.
Open the link from the *Application Routes* section (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com).
An *Evaluation Form* opens.

### Accessing Flagship UI

The Flagship UI will be accessed multiple times throughout this tutorial.
Login to Flagship and select an Account and Environment from the header.
The unique identifier of a flag is the combination of Use Case slug + flag key.
Those two will be required in the demo application in order to evaluate a flag.

### 9. Evaluate a missing Feature Flag

> **Note**: Expect the feature flag to be missing.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate a feature flag with random name and campaign
   (for example, type in
   'my-campaign' for Flagship Campaign (slug),
   'my-flag' for Flag Name and
   '<your-visitor-id>' for Visitor ID).
   The result should state that the feature flag with the given name is missing.

### 10. Create a new boolean Feature Flag

1. Open the Flagship UI as described [here](#accessing-flagship-ui).
2. Go to **Dashboard** from the Sidebar.
3. Select a Project and click on *Create a use case*.
4. Chose the **Toggle** template.
5. Enter name, description and slug and click on **Save and continue**.
6. Give a name to the default scenario, make sure **All users** are targeted and
   give key, type(Boolean) and value(false) to flag and click on **Save and continue**.
7. Lastly, go back to **Dashboard** and enable the flag.

### 11. Evaluate the newly created boolean Feature Flag

> **Note**: Expect the variation to be false.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate the boolean feature flag by entering its slug, key and your visitor ID.
   The result should state that the feature flag is of type *BOOLEAN* and its value is *false*.

### 12. Enable the boolean Feature Flag

1. Open the Flagship UI as described [here](#accessing-flagship-ui).
2. Go to **Dashboard** from the Sidebar.
3. Select the Project and click on *Edit* Use Case.
4. Go to **Scenarios**.
5. Change the flag value to true.
6. Save.

### 13. Verify that the boolean Feature Flag is enabled

> **Note**: Expect the feature flag to be enabled.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate the boolean feature flag by entering its slug, key and your visitor ID.
   The result should state that the feature flag is of type *BOOLEAN* and its value is *true*.

### 14. Create a new string Feature Flag

1. Open the Flagship UI as described [here](#accessing-flagship-ui).
2. Go to **Dashboard** from the Sidebar.
3. Select a Project and click on *Create a use case*.
4. Chose the **Toggle** template.
5. Enter name, description and slug and click on **Save and continue**.
6. Give a name to the default scenario, make sure **All users** are targeted and
   give key, type(Text) and value(variation-when-inactive) to flag and click on **Save and continue**.
7. Lastly, go back to **Dashboard** and enable the flag.

### 15. Evaluate the newly created string Feature Flag

> **Note**: Expect the variation to be *variation-when-inactive*.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate the string feature flag by entering its slug, key and your visitor ID.
   The result should state that the feature flag is of type *STRING* and its value is *variation-when-inactive*.

### 16. Enable the string Feature Flag

1. Open the Flagship UI as described [here](#accessing-flagship-ui).
2. Go to **Dashboard** from the Sidebar.
3. Select the Project and click on *Edit* Use Case.
4. Go to **Scenarios**.
5. Change the flag value to *variation-when-active*.
6. Save.

### 17. Verify that the string Feature Flag is enabled

> **Note**: Expect the variation to be *variation-when-active*.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate the string feature flag by entering its slug, key and your visitor ID.
   The result should state that the feature flag is of type *STRING* and its value is *variation-when-active*.

### 18. Specify direct delivery strategy of a variation of the string flag

1. Open the Flagship UI as described [here](#accessing-flagship-ui).
2. Go to **Dashboard** from the Sidebar.
3. Select the Project and click on *Edit* Use Case.
4. Go to **Scenarios**.
5. Change the targeting from *All users* to *Users by ID*,
   chose operator *Is*, enter visitor ID e.g. *special-visitor-1* and
   change the value of the flag to *variation-when-special*
6. Create new scenario with targeting *Users by ID*, chose operator *Is not*,
   enter the same visitor ID from the previous step and set the same flag key and type,
   then set the value to *variation-when-normal*.
7. Save.

### 19. Evaluate the string Feature Flag

> **Note**: Expect the variation to be *variation-when-special*.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate the string feature flag by entering its slug, key and *special-visitor-1* for visitor ID.
   The result should state that the feature flag is of type *STRING* and its value is *variation-when-special*.
3. Evaluate the string feature flag by entering its slug, key and any visitor ID other than *special-visitor-1*.
   The result should state that the feature flag is of type *STRING* and its value is *variation-when-normal*.
