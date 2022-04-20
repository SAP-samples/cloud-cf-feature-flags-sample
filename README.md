[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-feature-flags-sample)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-feature-flags-sample)

# Flagship Demo Application

Flagship Demo Application is a simple Spring Boot application that consumes [Flagship](https://www.flagship.io/) on SAP Cloud Platform, Cloud Foundry environment. It implements a [feature toggle](https://en.wikipedia.org/wiki/Feature_toggle) (evaluation call to Flagship) and exposes this feature toggle through a Web user interface. There is also one REST end-point that reads the value of `VCAP_SERVICES` environment variable.

## Prerequisites

* have set up [Maven 3.0.x](http://maven.apache.org/install.html)
* have an [SAP Cloud Platform trial account on Cloud Foundry environment](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/e50ab7b423f04a8db301d7678946626e.html)
* have a [trial space on a Cloud Foundry instance](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/fa5deb9cc4be4ca58070456cd2c47647.html#loioe9aed07891e545dd88192df013646897)
* have set up a [curl](https://curl.haxx.se/download.html) plug-in for cmd
* have [installed cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html)
* have an account in [Flagship](https://app.flagship.io/)

## Running the Application on SAP Cloud Platform

Follow these steps to run the Flagship Demo application on SAP Cloud Platform, Cloud Foundry environment.

> **Note:** This guide uses the Cloud Foundry trial account on Europe (Frankfurt) region (https://account.hanatrial.ondemand.com/cockpit#/home/overview). If you want to use a different region, you have to modify the domain in the requests. For more information about regions and hosts on SAP Cloud Platform, Cloud Foundry environment, see [Regions and Hosts](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html).

<!-- toc -->

- [1. Build the feature-flags-demo Application](#1-build-the-feature-flags-demo-application)
- [2. Edit application name in manifest file](#2-edit-application-name-in-manifest-file)
- [3. Deploy feature-flags-demo on SAP Cloud Platform](#3-deploy-feature-flags-demo-on-sap-cloud-platform)
- [4. Create a user provided service with Flagship connection information](#4-create-a-user-provided-service-with-flagship-connection-information)
  * [4.1 Get Flagship environment id and API key](#41-get-flagship-environment-id-and-api-key)
  * [4.2 Create a user provided service](#42-create-a-user-provided-service)
- [5. Call the feature-flags-demo Application's /vcap_services End-Point](#5-call-the-feature-flags-demo-applications-vcap_services-end-point)
- [6. Bind feature-flags-demo to flagship-instance](#6-bind-feature-flags-demo-to-flagship-instance)
- [7. Restage feature-flags-demo](#7-restage-feature-flags-demo)
- [8. Ensure that flagship-instance is bound to feature-flags-demo](#8-ensure-that-flagship-instance-is-bound-to-feature-flags-demo)
- [Accessing the Demo Application](#accessing-the-demo-application)
- [Accessing the Feature Flags Dashboard](#accessing-the-feature-flags-dashboard)
- [9. Evaluate a Missing Feature Flag](#9-evaluate-a-missing-feature-flag)
- [10. Create a New Boolean Feature Flag](#10-create-a-new-boolean-feature-flag)
- [11. Evaluate the Newly Created Boolean Feature Flag](#11-evaluate-the-newly-created-boolean-feature-flag)
- [12. Enable the Boolean Feature Flag](#12-enable-the-boolean-feature-flag)
- [13. Verify that the Boolean Feature Flag is Enabled](#13-verify-that-the-boolean-feature-flag-is-enabled)
- [14. Create a New String Feature Flag](#14-create-a-new-string-feature-flag)
- [15. Evaluate the Newly Created String Feature Flag](#15-evaluate-the-newly-created-string-feature-flag)
- [16. Enable the String Feature Flag](#16-enable-the-string-feature-flag)
- [17. Verify that the String Feature Flag is Enabled](#17-verify-that-the-string-feature-flag-is-enabled)
- [18. Specify Direct Delivery Strategy of a Variation of the String Flag](#18-specify-direct-delivery-strategy-of-a-variation-of-the-string-flag)
- [19. Evaluate the String Feature Flag Using Identifier](#19-evaluate-the-string-feature-flag-using-identifier)

<!-- tocstop -->

### 1. Build the feature-flags-demo Application

    $ git clone git@github.com:SAP/cloud-cf-feature-flags-sample.git
    $ cd cloud-cf-feature-flags-sample
    $ mvn clean install

> **Note:** Alternatively, you can use the Eclipse IDE, use the `clean install` goal from _Run As > Maven Build..._ menu.

### 2. Edit application name in manifest file

Due to CloudFoundry's limitiation in regards to application naming it's quite possible for someone to have already deployed the demo application with the **feature-flags-demo** name as it is currently set in the **manifest.yml** file. CloudFoundry will not allow another application with the same name to be deployed, so you **MUST** edit the manifest file and change the application name before deploying. For example:

    ---
    applications:
    - name: feature-flags-demo123
      path: target/feature-flags-demo.jar

> **Note:** Use the modified value in the commands which require application name (e.g. cf bind-service)
and when requesting the application in the browser or via curl.

### 3. Deploy feature-flags-demo on SAP Cloud Platform

    $ cf api https://api.cf.eu10.hana.ondemand.com
    $ cf login
    $ cf push

### 4. Create a user provided service with Flagship connection information

#### 4.1 Get Flagship environment id and API key

1. Login to [Flagship](https://app.flagship.io/) and open _Settings_ -> _Environment Settings_.
2. Copy the corresponding environment id and API key.

#### 4.2 Create a user provided service

Execute the following command using the environment id and API key from the previous step.

    $ cf create-user-provided-service flagship-instance -t flagship-flags-service -p "{ \"base-uri\": \"https://decision.flagship.io\", \"env-id\": \"<env-id>\", \"api-key\": \"<api-key>\" }"

    -----
    Creating user provided service flagship-instance in org <ORG_ID> / space dev as <USER_ID>...
    OK

### 5. Call the feature-flags-demo Application's /vcap_services End-Point

> **Note**: Expect to receive an empty JSON.

The /vcap_services end-point simply returns the content of  _VCAP_SERVICES_ environment variable. As for now there is no service instances bound to `feature-flags-demo`, so you will receive an empty JSON.

In the command you use the following URL: \<application_URL\>/vcap_services. You can find the \<application_URL\> in the SAP Cloud Platform Cockpit, in the _feature-flag-demo > Overview > Application Routes_.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services

### 6. Bind feature-flags-demo to flagship-instance

    $ cf bind-service feature-flags-demo flagship-instance

    -----
    Binding service flagship-instance to app feature-flags-demo in org <ORG_ID> / space dev as <USER_ID>...
    OK
    TIP: Use 'cf restage feature-flags-demo' to ensure your env variable changes take effect

### 7. Restage feature-flags-demo

Restage `feature-flags-demo` application so the changes in the application environment take effect.

    $ cf restage feature-flags-demo

### 8. Ensure that flagship-instance is bound to feature-flags-demo

> **Note**: Expect to receive the injected environment variables by the Feature Flags service.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services


Sample JSON response:
```json
{
  "feature-flags": [
    {
      "credentials": {

      },

      "tags": [
        "flagship-flags-service"
      ]
    }
  ]
}
```

### Accessing the Demo Application

The web interface of the demo application will be accessed multiple times throughout this tutorial.
Here is how to open it: navigate to feature-flags-demo application overview in the SAP Cloud Platform Cockpit.
Open the link from the _Application Routes_ section (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com).
An _Evaluation Form_ opens.

### Accessing the Feature Flags Dashboard

The Feature Flags dashboard will be accessed multiple times throughout this tutorial.
Here is how to open it via the SAP Cloud Platform Cockpit: navigate to your subaccount,
subscribe to the Feature Flags dashboard via creating an instance of the Feature Flags service, plan _dashboard_ if haven't done so already.
Access Feature Flags dashboard from the list of subscribed applications.
Select the service instance you are currently working with.

The dashboard could be accessed directly via URL like https://<subdomain\>.feature-flags-dashboard.cfapps.eu10.hana.ondemand.com/manageinstances/<instance-id\>.
The instance ID is a unique ID of the service instance.

### 9. Evaluate a Missing Feature Flag

> **Note**: Expect the feature flag to be missing.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate a feature flag with random name (for example, type in 'my-boolean-flag').
The result should state that the feature flag with the given name is missing.

### 10. Create a New Boolean Feature Flag

1. Open the Feature Flags dashboard as described [here](#accessing-the-feature-flags-dashboard).
2. Choose _New Flag_.
3. Fill in the required fields (for example, 'my-boolean-flag' for _Name_, 'Super cool feature' for _Description_ and 'OFF' for _State_).
4. Choose _Save_.

### 11. Evaluate the Newly Created Boolean Feature Flag

> **Note**: Expect the variation to be false.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the boolean feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the newly created feature flag.
The result should state that the feature flag is of type _BOOLEAN_ and its variation is _false_.

### 12. Enable the Boolean Feature Flag

1. Open the Feature Flags dashboard as described [here](#accessing-the-feature-flags-dashboard).
2. Enable the boolean feature flag using the switch in the _Active_ column.

### 13. Verify that the Boolean Feature Flag is Enabled

> **Note**: Expect the feature flag to be enabled.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the boolean feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _BOOLEAN_ and its variation is _true_.

### 14. Create a New String Feature Flag

1. Open the Feature Flags dashboard as described [here](#accessing-the-feature-flags-dashboard).
2. Choose _New Flag_.
3. Fill in the required fields (for example, 'my-string-flag' for _Name_, 'Coolest of features' for _Description_, choose _String_ as _Flag Type_ and 'OFF' for _State_).
Enter the following values as different variations of the flag:
  - _Var. 1_: _variation-when-inactive_
  - _Var. 2_: _variation-when-active_
  - _Var. 3_ (choose the add button (with a '+' sign) to add a field for it): _variation-for-friends-and-family_
4. Select _Var. 2_ in the _Deliver_ combobox in the _Default Variation_ section.
5. Choose _Save_.

### 15. Evaluate the Newly Created String Feature Flag

> **Note**: Expect the variation to be _variation-when-inactive_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the newly created feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-when-inactive_.

### 16. Enable the String Feature Flag

1. Open the Feature Flags dashboard as described [here](#accessing-the-feature-flags-dashboard).
2. Enable the string feature flag using the switch in the _Active_ column.

### 17. Verify that the String Feature Flag is Enabled

> **Note**: Expect the variation to be _variation-when-active_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-when-active_.

### 18. Specify Direct Delivery Strategy of a Variation of the String Flag

1. Open the Feature Flags dashboard as described [here](#accessing-the-feature-flags-dashboard).
2. Select the string feature flag.
3. Set it to edit mode via choosing the _Edit Flag_ button in the toolbar.
4. Go to _Strategy_ section, _Direct Delivery_ sub-section and choose the button with '+' sign.
5. Select _Var. 3_ from the combobox and enter _friends-and-family_ in the text input.
6. Choose _Save_.

### 19. Evaluate the String Feature Flag Using Identifier

> **Note**: Expect the variation to be _variation-for-friends-and-family_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag name in the _Feature Flag Name_ field,
enter _friends-and-family_ in the _Identifier (optional)_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-for-friends-and-family_.

> **Note**: Once direct delivery is configured, Feature Flags service requires providing an identifier.
An error is returned if such is not present.
Variation _variation-when-active_ is returned for all identifiers
except those explicitly configured in the Feature Flags dashboard
for which the provided rules apply (like for the _friends-and-family_ identifier).
