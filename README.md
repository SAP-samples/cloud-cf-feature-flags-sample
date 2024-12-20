[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-feature-flags-sample)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-feature-flags-sample)

# Feature Flags Service Demo Application

## Description

Feature Flags service Demo Application is a simple Spring Boot application that consumes the [Feature Flags service](https://help.sap.com/viewer/2250efa12769480299a1acd282b615cf/Cloud/en-US) on SAP BTP, Cloud Foundry environment. It implements a [feature toggle](https://en.wikipedia.org/wiki/Feature_toggle) (evaluation call to the Feature Flags service) and exposes this feature toggle through a Web user interface. There is also one REST end-point that reads the value of `VCAP_SERVICES` environment variable.

## Requirements

* You have set up [Maven 3.0.x](http://maven.apache.org/install.html).
* You have an [SAP BTP enterprise (productive) account](https://help.sap.com/docs/btp/sap-business-technology-platform/getting-started-with-enterprise-account-in-cloud-foundry-environment) on Cloud Foundry environment. 

  💡**NOTE:** You can also use a [trial account](https://help.sap.com/docs/btp/sap-business-technology-platform/getting-started-with-trial-account-in-cloud-foundry-environment) but some functionalities won't be available for you.
* You have a space on a Cloud Foundry instance - [productive](https://help.sap.com/docs/btp/sap-business-technology-platform/create-spaces) or [trial](https://help.sap.com/docs/btp/sap-business-technology-platform/cf-env-setting-up-your-trial-account#create-your-trial-space).
* You have set up the [curl](https://curl.haxx.se/download.html) plug-in for `cmd`.
* You have installed [cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html).

## Running the Application on SAP BTP

💡**NOTE:** This guide uses the **eu20** region (https://emea.cockpit.btp.cloud.sap/cockpit#). 

> To use a different SAP BTP region, you need to modify the domain in the requests. See: [Cloud Foundry Regions and API Endpoints](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html)

Follow the steps below to run `Feature Flags Demo Application` on SAP BTP, Cloud Foundry environment.

<!-- toc -->

  * [1. Build the feature-flags-demo Application](#1-build-the-feature-flags-demo-application)
  * [2. Edit application name in manifest file](#2-edit-application-name-in-manifest-file)
  * [3. Deploy feature-flags-demo on SAP BTP](#3-deploy-feature-flags-demo-on-sap-btp)
  * [4. Create a Service Instance of Feature Flags service](#4-create-a-service-instance-of-feature-flags-service)
    + [4.1 Ensure the feature-flags service exists in the Service Marketplace](#41-ensure-the-feature-flags-service-exists-in-the-service-marketplace)
    + [4.2 Create a Service Instance of Feature Flags with Plan `standard`](#42-create-a-service-instance-of-feature-flags-with-plan-standard)
  * [5. Call the feature-flags-demo Application's /vcap_services End-Point](#5-call-the-feature-flags-demo-applications-vcap_services-end-point)
  * [6. Bind feature-flags-demo to feature-flags-instance](#6-bind-feature-flags-demo-to-feature-flags-instance)
  * [7. Restage feature-flags-demo](#7-restage-feature-flags-demo)
  * [8. Ensure that feature-flags-instance is bound to feature-flags-demo](#8-ensure-that-feature-flags-instance-is-bound-to-feature-flags-demo)
  * [Accessing the Demo Application](#accessing-the-demo-application)
  * [Accessing the Feature Flags Dashboard](#accessing-the-feature-flags-dashboard)
  * [9. Evaluate a Missing Feature Flag](#9-evaluate-a-missing-feature-flag)
  * [10. Create a New Boolean Feature Flag](#10-create-a-new-boolean-feature-flag)
  * [11. Evaluate the Newly Created Boolean Feature Flag](#11-evaluate-the-newly-created-boolean-feature-flag)
  * [12. Enable the Boolean Feature Flag](#12-enable-the-boolean-feature-flag)
  * [13. Verify that the Boolean Feature Flag is Enabled](#13-verify-that-the-boolean-feature-flag-is-enabled)
  * [14. Create a New String Feature Flag](#14-create-a-new-string-feature-flag)
  * [15. Evaluate the Newly Created String Feature Flag](#15-evaluate-the-newly-created-string-feature-flag)
  * [16. Enable the String Feature Flag](#16-enable-the-string-feature-flag)
  * [17. Verify that the String Feature Flag is Enabled](#17-verify-that-the-string-feature-flag-is-enabled)
  * [18. Specify Direct Delivery Strategy of a Variation of the String Flag](#18-specify-direct-delivery-strategy-of-a-variation-of-the-string-flag)
  * [19. Evaluate the String Feature Flag Using Identifier](#19-evaluate-the-string-feature-flag-using-identifier)
- [Contributing](#contributing)
- [Code of Conduct](#code-of-conduct)
- [Licensing](#licensing)

<!-- tocstop -->

### 1. Build the feature-flags-demo Application

Run the following commands, consequently:

    $ git clone git@github.com:SAP/cloud-cf-feature-flags-sample.git
    $ cd cloud-cf-feature-flags-sample
    $ mvn clean install

> **Note:** Alternatively, you can use the Eclipse IDE. Choose the `clean install` goal from the _Run As > Maven Build..._ menu.

### 2. Edit application name in manifest file

It's quite possible that someone else has already deployed the `Feature Flags Demo Application` with the name **feature-flags-demo** (as it is currently set in the `manifest.yml` file). 
Cloud Foundry does not allow two applications with the same name to be deployed in the same region! Therefore, we **highly recommend** that you change the application name in the `manifest.yml` file before deploying. For example:

    ---
    applications:
    - name: feature-flags-demo123
      path: target/feature-flags-demo.jar

💡**NOTE:** Use the modified value in all commands that require application name, as well as when requesting the application in a browser or via `curl`.

### 3. Deploy feature-flags-demo on SAP BTP

 1. Log in to your Cloud Foundry landscape. (The `eu20` region is just an example.) Run:

    ```
    $ cf login -a https://api.cf.eu20.hana.ondemand.com
    ```

2. Choose your subaccount (org) and space.

3. Deploy the application. Run:

    ```
    $ cf push
    ```

### 4. Create a Service Instance of Feature Flags service

#### 4.1 Ensure the feature-flags service exists in the Service Marketplace

Run the following command:   
  ```
  $ cf marketplace
  ```  
Result:


    -----
    Getting services from marketplace in org <ORG_ID> / space <SPACE> as <USER_ID>...
    OK
    service          	plans    	description
    ...
    feature-flags    	lite, standard     	Feature Flags service for controlling feature rollout
    ...

**NOTE:**  If you're using a trial account, the only available service plan will be `lite`.

#### 4.2 Create a Service Instance of Feature Flags

* For a productive account, run:
  
    ```
    $ cf create-service feature-flags standard feature-flags-instance
    ```

* For a trial account, run:

    ```
    $ cf create-service feature-flags lite feature-flags-instance
    ```


**Note:** Alternatively, you can use the SAP BTP cockpit. See [Create a Service Instance](https://help.sap.com/docs/feature-flags-service/sap-feature-flags-service/initial-setup#loioc7b30b5bf54149148d2302617917dc3e).


### 5. Call the feature-flags-demo Application's /vcap_services End-Point

> **Note**: Expect to receive an empty JSON code - {}.

The `/vcap_services` end-point simply returns the content of the _VCAP_SERVICES_ environment variable. As for now, there is no service instance bound to `feature-flags-demo`, thus you receive an empty JSON.

In the command you use the following URL: `\<application_URL\>/vcap_services`. 

To find the value of `\<application_URL\>`, go to the SAP BTP cockpit > **feature-flag-demo** > **Overview** > **Application Routes**.

To call the application, run (for example):

    $ curl https://feature-flags-demo.cfapps.eu20.hana.ondemand.com/vcap_services

### 6. Bind feature-flags-demo to feature-flags-instance

    $ cf bind-service feature-flags-demo feature-flags-instance

    -----
    Binding service feature-flags-instance to app feature-flags-demo in org <ORG_ID> / space <SPACE> as <USER_ID>...
    OK
    TIP: Use 'cf restage feature-flags-demo' to ensure your env variable changes take effect

> **Note:** Alternatively, you can use the SAP BTP cockpit. See [Bind Your Application to the SAP Feature Flags Service Instance](https://help.sap.com/docs/feature-flags-service/sap-feature-flags-service/initial-setup#loioe7ef0ce6d4b14ae387de5bb18549c250).

### 7. Restage feature-flags-demo

Restage `feature-flags-demo` application so the changes in the application environment take effect.

    $ cf restage feature-flags-demo

### 8. Ensure that feature-flags-instance is bound to feature-flags-demo

> **Note**: Expect to receive the injected environment variables by the Feature Flags service.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services


Sample JSON response:
```json
{
  "feature-flags": [
    {
      "credentials": {
        "x509": {
          "certificate": "...",
          "key": "...",
          "clientid": "...",
          "...": "..."
        },
        "password": "aa_12345678XYZZZZZ000mnopRE+qs0e=",
        "uri": "https://feature-flags.cfapps.eu20.hana.ondemand.com",
        "username": "sbss_x234osj//pmabsuskr6nshmb2arw6dld4hfb3cj4m2bonkqmm3ts6c68mdpzxz2fma="
      },
      "syslog_drain_url": null,
      "volume_mounts": [ ],
      "label": "feature-flags",
      "provider": null,
      "plan": "standard",
      "name": "feature-flags-instance",
      "tags": [
        "feature-flags"
      ]
    }
  ]
}
```

### Accessing the Demo Application

The web interface of the demo application will be accessed multiple times throughout this tutorial. Here is how to open it: 

1. Go to the SAP BTP cockpit.
2. Navigate to the `feature-flags-demo` application overview.
3. Open the link from the **Application Routes** section. For example: `https://feature-flags-demo-happy-bear.cfapps.eu20.hana.ondemand.com`
4. The **Evaluation Form** opens.

### Accessing the Feature Flags Dashboard

The `Feature Flags Dashboard` will be accessed multiple times throughout this tutorial. Here is how to open it: 

1. Go to the SAP BTP cockpit.
2. Navigate to your subaccount.
3. Create a subscription to **Feature Flags Dashboard**. To do that, create an instance of the Feature Flags service, with plan `dashboard`.
4. Access **Feature Flags Dashboard** from the list of subscribed applications.
5. Select the service instance you are currently working with.

💡**NOTE:** The dashboard URL always has the following pattern: 

`https://<subdomain\>.feature-flags-dashboard.cfapps.eu20.hana.ondemand.com/manageinstances/<instance-id\>`

> The <instance-id> is a unique ID of your Feature Flag service instance.

### 9. Evaluate a Missing Feature Flag

> **Note**: Expect the feature flag to be missing.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter a feature flag with a random name, for example `my-boolean-flag`.
3. Choose **Evaluate**.
4. The result should state that a feature flag with this name is missing.

### 10. Create a New Boolean Feature Flag

1. Open the `Feature Flags Dashboard` as described [here](#accessing-the-feature-flags-dashboard).
2. Choose **New Flag**.
3. Fill in the required fields. For example:
   * **Name**:  `my-boolean-flag`
   * **Description**: `Super cool feature`
   * **State**: `OFF`
5. Choose **Save**.

### 11. Evaluate the Newly Created Boolean Feature Flag

> **Note**: Expect the variation to be `false`.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the Boolean feature flag name.
3. Choose **Evaluate**.
4. The result should state that the feature flag is of type `BOOLEAN` and its variation is `false`.

### 12. Enable the Boolean Feature Flag

1. Open the `Feature Flags Dashboard` as described [here](#accessing-the-feature-flags-dashboard).
2. Enable the Boolean feature flag using the switch in the **Active** column.

### 13. Verify that the Boolean Feature Flag is Enabled

> **Note**: Expect the variation to be `true`.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the Boolean feature flag name.
3. Choose **Evaluate**.
4. The result should state that the feature flag is of type `BOOLEAN` and its variation is `true`.

> ### CAUTION!
> The next procedures (14 - 19) are only applicable for productive accounts, which means plan `standard`.

### 14. Create a New String Feature Flag

1. Open the `Feature Flags Dashboard` as described [here](#accessing-the-feature-flags-dashboard).
2. Choose **New Flag**.
3. Fill in the required fields. For example:
    * **Name**: `my-string-flag`
    * **Description**: `Coolest of features`
    * **Type**: `String`
    * **State**: `OFF`
4. Enter the following values as different variations of the flag:
    * **Var. 1**: `variation-when-inactive`
    * **Var. 2**: `variation-when-active`
    * **Var. 3** (choose the **Add** button with the '+' sign to add a new field): `variation-for-friends-and-family`
5. From the **Default Variation**, open the **Deliver** combo box and select `Var. 2`.
6. Choose **Save**.

### 15. Evaluate the Newly Created String Feature Flag

> **Note**: Expect the variation to be `variation-when-inactive`.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name.
3. Choose **Evaluate**.
4. The result should state that the feature flag is of type `STRING` and its variation is `variation-when-inactive`.

### 16. Enable the String Feature Flag

1. Open the `Feature Flags Dashboard` as described [here](#accessing-the-feature-flags-dashboard).
2. Enable the string feature flag using the switch in the **Active** column.

### 17. Verify that the String Feature Flag is Enabled

> **Note**: Expect the variation to be `variation-when-active`.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name.
3. Choose **Evaluate**.
4. The result should state that the feature flag is of type `STRING` and its variation is `variation-when-active`.

### 18. Specify Direct Delivery Strategy of a Variation of the String Flag

1. Open the `Feature Flags Dashboard` as described [here](#accessing-the-feature-flags-dashboard).
2. Select the string feature flag.
3. Choose the **Edit Flag** button.
4. Go to the **Strategy** section, subsection **Direct Delivery**, and choose the '+' button.
5. Select **Var. 3** from the combobox and enter `friends-and-family` in the text input.
6. Choose **Save**.

### 19. Evaluate the String Feature Flag Using Identifier

> **Note**: Expect the variation to be `variation-for-friends-and-family`.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name.
3. For the **Identifier (optional)** field, enter `friends-and-family`.
4. Choose **Evaluate**.
5. The result should state that the feature flag is of type `STRING` and its variation is `variation-for-friends-and-family`.

> Once **Direct Delivery** is configured, the Feature Flags service requires providing an identifier. If such is not present, an error is thrown.

💡**NOTE:** `variation-when-active` is returned for all identifiers - except for those explicitly configured in the `Feature Flags Dashboard` for which the provided rules apply (like the `friends-and-family` identifier).

## Contributing

Refer to the [contrubuting guideline](/CONTRIBUTING.md).

## Code of Conduct
Refer to the [SAP Open Source Code of Conduct](https://github.com/SAP-samples/.github/blob/main/CODE_OF_CONDUCT.md).

## Licensing
See [LICENSE](/LICENSE) file.
