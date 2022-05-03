[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-feature-flags-sample)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-feature-flags-sample)

# Feature Flags via LaunchDarkly Demo Application

Feature Flags Demo Application is a simple Spring Boot application that consumes feature flags via [LaunchDarkly](https://launchdarkly.com/).
There is also one REST end-point that reads the value of `VCAP_SERVICES` environment variable.

## Prerequisites

* have set up [Maven 3.0.x](http://maven.apache.org/install.html)
* have an [SAP Cloud Platform trial account on Cloud Foundry environment](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/e50ab7b423f04a8db301d7678946626e.html)
* have a [trial space on a Cloud Foundry instance](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/fa5deb9cc4be4ca58070456cd2c47647.html#loioe9aed07891e545dd88192df013646897)
* have set up a [curl](https://curl.haxx.se/download.html) plug-in for cmd
* have [installed cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html)
* have an account in [LaunchDarkly](https://launchdarkly.com/)

## Running the Application on SAP Cloud Platform

Follow these steps to run the Feature Flags Demo application on SAP Cloud Platform, Cloud Foundry environment.

> **Note:** This guide uses the Cloud Foundry trial account on Europe (Frankfurt) region (https://account.hanatrial.ondemand.com/cockpit#/home/overview). If you want to use a different region, you have to modify the domain in the requests. For more information about regions and hosts on SAP Cloud Platform, Cloud Foundry environment, see [Regions and Hosts](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html).

<!-- toc -->

- [1. Build the feature-flags-demo Application](#1-build-the-feature-flags-demo-application)
- [2. Edit application name in manifest file](#2-edit-application-name-in-manifest-file)
- [3. Deploy feature-flags-demo on SAP Cloud Platform](#3-deploy-feature-flags-demo-on-sap-cloud-platform)
- [4. Create a user provided service containing LaunchDarkly SDK key](#4-create-a-user-provided-service-containing-launchdarkly-sdk-key)
  * [4.1 Get LaunchDarkly SDK key](#41-get-launchdarkly-sdk-key)
  * [4.2 Create a user provided service](#42-create-a-user-provided-service)
- [5. Call the feature-flags-demo Application's /vcap_services End-Point](#5-call-the-feature-flags-demo-applications-vcap_services-end-point)
- [6. Bind feature-flags-demo to ld-instance](#6-bind-feature-flags-demo-to-ld-instance)
- [7. Restage feature-flags-demo](#7-restage-feature-flags-demo)
- [8. Ensure that ld-instance is Bound to feature-flags-demo](#8-ensure-that-ld-instance-is-bound-to-feature-flags-demo)
- [Accessing the Demo Application](#accessing-the-demo-application)
- [Accessing LaunchDarkly UI](#accessing-launchdarkly-ui)
- [9. Evaluate a Missing Feature Flag](#9-evaluate-a-missing-feature-flag)
- [10. Create a New Boolean Feature Flag](#10-create-a-new-boolean-feature-flag)
- [11. Evaluate the Newly Created Boolean Feature Flag](#11-evaluate-the-newly-created-boolean-feature-flag)
- [12. Enable the Boolean Feature Flag](#12-enable-the-boolean-feature-flag)
- [13. Verify that the Boolean Feature Flag is Enabled](#13-verify-that-the-boolean-feature-flag-is-enabled)
- [14. Create a New String Feature Flag](#14-create-a-new-string-feature-flag)
- [15. Evaluate the Newly Created String Feature Flag](#15-evaluate-the-newly-created-string-feature-flag)
- [16. Enable the String Feature Flag](#16-enable-the-string-feature-flag)
- [17. Verify that the String Feature Flag is Enabled](#17-verify-that-the-string-feature-flag-is-enabled)
- [18. Specify Rule for Variation Delivery of the String Flag](#18-specify-rule-for-variation-delivery-of-the-string-flag)
- [19. Evaluate the String Feature Flag Using Identifier](#19-evaluate-the-string-feature-flag-using-identifier)

<!-- tocstop -->

### 1. Build the feature-flags-demo Application

    $ git clone git@github.com:SAP/cloud-cf-feature-flags-sample.git
    $ cd cloud-cf-feature-flags-sample
    $ mvn clean install

> **Note:** Alternatively, you can use the Eclipse IDE, use the `clean install` goal from _Run As > Maven Build..._ menu.

### 2. Edit application name in manifest file

Due to CloudFoundry's limitiation in regards to application naming it's quite possible for someone to have already deployed the Feature Flags demo application with the **feature-flags-demo** name as it is currently set in the **manifest.yml** file. CloudFoundry will not allow another application with the same name to be deployed, so you **MUST** edit the manifest file and change the application name before deploying. For example:

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

### 4. Create a user provided service containing LaunchDarkly SDK key

#### 4.1 Get LaunchDarkly SDK key

1. Login to LaunchDarkly and go to your [projects page](https://app.launchdarkly.com/settings/projects).
2. Choose a project and an environment and copy the corresponding SDK key.

#### 4.2 Create a user provided service

Execute the following command using the SDK key from the previous step.

    $ cf create-user-provided-service ld-instance -t launchdarkly-flags-service -p "{ \"sdk-key\": \"<sdk-key>\" }"

    -----
    Creating user provided service ld-instance in org <ORG_ID> / space dev as <USER_ID>...
    OK

### 5. Call the feature-flags-demo Application's /vcap_services End-Point

> **Note**: Expect to receive an empty JSON.

The /vcap_services end-point simply returns the content of  _VCAP_SERVICES_ environment variable. As for now there is no service instances bound to `feature-flags-demo`, so you will receive an empty JSON.

In the command you use the following URL: \<application_URL\>/vcap_services. You can find the \<application_URL\> in the SAP Cloud Platform Cockpit, in the _feature-flag-demo > Overview > Application Routes_.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services

### 6. Bind feature-flags-demo to ld-instance

    $ cf bind-service feature-flags-demo ld-instance

    -----
    Binding service ld-instance to app feature-flags-demo in org <ORG_ID> / space dev as <USER_ID>...
    OK
    TIP: Use 'cf restage feature-flags-demo' to ensure your env variable changes take effect

### 7. Restage feature-flags-demo

Restage `feature-flags-demo` application so the changes in the application environment take effect.

    $ cf restage feature-flags-demo

### 8. Ensure that ld-instance is Bound to feature-flags-demo

> **Note**: Expect to receive the injected environment variables.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services


Sample JSON response:
```json
{
  "user-provided": [
    {
      "label": "user-provided",
      "name": "ld-instance",
      "tags": [
        "launchdarkly-flags-service"
      ],
      "instance_guid": "...",
      "instance_name": "ld-instance",
      "binding_guid": "...",
      "binding_name": null,
      "credentials": {
        "sdk-key": "<sdk-key>"
      },
      "syslog_drain_url": "",
      "volume_mounts": []
    }
  ]
}
```

### Accessing the Demo Application

The web interface of the demo application will be accessed multiple times throughout this tutorial.
Here is how to open it: navigate to feature-flags-demo application overview in the SAP Cloud Platform Cockpit.
Open the link from the _Application Routes_ section (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com).
An _Evaluation Form_ opens.

### Accessing LaunchDarkly UI

The LaunchDarkly UI will be accessed multiple times throughout this tutorial.
Login to LaunchDarkly and go to the _Feature flags_ tab in the selected project and environment.

### 9. Evaluate a Missing Feature Flag

> **Note**: Expect the feature flag to be missing.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Evaluate a feature flag with random key (for example, type in 'my-boolean-flag').
The result should state that the feature flag with the given key is missing.

### 10. Create a New Boolean Feature Flag

1. Open _Feature flags_ in LaunchDarkly as described [here](#accessing-launchdarkly-ui).
2. Choose _Create flag_.
3. Fill in the required fields (for example, 'my-boolean-flag' for _Name_ and _Key_, 'Super cool feature' for _Description_, tick the _This is a permanent flag_ checkbox).
4. Choose _Save flag_.

### 11. Evaluate the Newly Created Boolean Feature Flag

> **Note**: Expect the variation to be false.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the boolean feature flag key in the _Feature Flag Key_ field and choose _Evaluate_.
3. Evaluate the newly created feature flag.
The result should state that the feature flag is of type _BOOLEAN_ and its variation is _false_.

### 12. Enable the Boolean Feature Flag

1. Open _Feature flags_ in LaunchDarkly as described [here](#accessing-launchdarkly-ui).
2. Enable the boolean feature flag using the switch in the row for the flag. Choose _Save changes_.

### 13. Verify that the Boolean Feature Flag is Enabled

> **Note**: Expect the feature flag to be enabled.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the boolean feature flag key in the _Feature Flag Key_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _BOOLEAN_ and its variation is _true_.

### 14. Create a New String Feature Flag

1. Open _Feature flags_ in LaunchDarkly as described [here](#accessing-launchdarkly-ui).
2. Choose _Create flag_.
3. Fill in the required fields (for example, 'my-string-flag' for _Name_ and _Key_, 'Coolest of features' for _Description_, choose _String_ as _Flag variations_).
Enter the following values as different variations of the flag:
  - _Variation 1_: _variation-when-active_
  - _Variation 2_: _variation-when-inactive_
  - _Variation 3_ (choose the _Add variation_ button to add a field for it): _variation-for-friends-and-family_
4. Tick the _This is a permanent flag_ checkbox.
5. Choose _Save flag_.

### 15. Evaluate the Newly Created String Feature Flag

> **Note**: Expect the variation to be _variation-when-inactive_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag key in the _Feature Flag Key_ field and choose _Evaluate_.
3. Evaluate the newly created feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-when-inactive_.

### 16. Enable the String Feature Flag

1. Open _Feature flags_ in LaunchDarkly as described [here](#accessing-launchdarkly-ui).
2. Enable the string feature flag using the switch in the row for the flag. Choose _Save changes_.

### 17. Verify that the String Feature Flag is Enabled

> **Note**: Expect the variation to be _variation-when-active_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag key in the _Feature Flag Key_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-when-active_.

### 18. Specify Rule for Variation Delivery of the String Flag

1. Open _Feature flags_ in LaunchDarkly as described [here](#accessing-launchdarkly-ui).
2. Select the string feature flag.
3. Choose _+ Add rules_ button.
4. Type _identifier_ in the _Select an attribute_ field (if it does not yet exist - create such an attribute).
5. Select _is one of_ in the _Select an operator_ field.
6. Enter _friends-and-family_ in the _Enter some values_ field (if it does not yet exist - create such a value).
7. Select _variation-for-friends-and-family_ as variation.
8. Choose _Save changes_.

### 19. Evaluate the String Feature Flag Using Identifier

> **Note**: Expect the variation to be _variation-for-friends-and-family_.

1. Open the demo application as described [here](#accessing-the-demo-application).
2. Enter the string feature flag key in the _Feature Flag Key_ field,
enter _friends-and-family_ in the _Identifier (optional)_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag is of type _STRING_ and its variation is _variation-for-friends-and-family_.

> **Note**: Variation _variation-when-active_ is returned for all identifiers
except those explicitly configured in the LaunchDarkly UI
for which the provided rules apply (like for the _friends-and-family_ identifier).
