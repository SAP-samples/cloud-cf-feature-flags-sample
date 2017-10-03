# Feature Flags Service Demo Application

Feature Flags service Demo Application is a simple Spring Boot application that consumes the [Feature Flags service](https://help.sap.com/viewer/2250efa12769480299a1acd282b615cf/Cloud/en-US) on SAP Cloud Platform, Cloud Foundry environment. It implements a [feature toggle](https://en.wikipedia.org/wiki/Feature_toggle) (evaluation call to the Feature Flags service) and exposes this feature toggle through a Web user interface. There is also one REST end-point that reads the value of `VCAP_SERVICES` environment variable.

## Prerequisites

* have set up [Maven 3.0.x](http://maven.apache.org/install.html)
* have an [SAP Cloud Platform trial account on Cloud Foundry environment](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/65d74d39cb3a4bf8910cd36ec54d2b99.html)
* have a [trial space on a Cloud Foundry instance](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/76e79d62fa0149d5aa7b0698c9a33687.html)
* have set up a [curl](https://curl.haxx.se/download.html) plug-in for cmd 
* have [installed cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html)

## Running the Application on SAP Cloud Platform

Follow these steps to run the Feature Flags Service Demo application on SAP Cloud Platform, Cloud Foundry environment.

> **Note:** This guide uses the Cloud Foundry trial account on Europe (Frankfurt) region (https://account.hanatrial.ondemand.com/cockpit#/home/overview). If you want to use a different region, you have to modify the domain in the requests. For more information about regions and hosts on SAP Cloud Platform, Cloud Foundry environment, see [Regions and Hosts](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html).

1. [Build the application.](#1-build-the-feature-flags-demo-application)
2. [Deploy the application.](#2-deploy-feature-flags-demo-on-sap-cloud-platform)
3. [Create a service instance of the Feature Flag service.](#3-create-a-service-instance-of-feature-flags-service)
4. [Call feature-flags-demo application /vcap_services end-point.](#4-call-the-feature-flags-demo-application-vcap_services-end-point)
5. [Bind feature-flags-demo to feature-flags-instance.](#5-bind-feature-flags-demo-to-feature-flags-instance)
6. [Restage feature-flags-demo application.](#6-restage-feature-flags-demo)
7. [Ensure that feature-flags-instance is properly bound to feature-flags-demo.](#7-ensure-that-feature-flags-instance-is-bound-to-feature-flags-demo)
8. [Perform an evaluation of missing feature flag.](#8-evaluate-if-the-feature-flag-is-missing)
9. [Create a new feature flag.](#9-create-a-new-feature-flag)
10. [Perform an evaluation of the newly created feature flag.](#10-evaluate-the-newly-created-feature-flag)
11. [Enable the feature flag.](#11-enable-the-feature-flag)
12. [Verify that the feature flag was properly enabled.](#12-verify-that-the-feature-flag-is-enabled)

### 1. Build the feature-flags-demo Application 

    $ git clone git@github.com:SAP/cloud-cf-feature-flags-sample.git
    $ cd cloud-cf-feature-flags-sample
    $ mvn clean install

> **Note:** Alternatively, you can use the Eclipse IDE, use the `clean install` goal from _Run As > Maven Build..._ menu.

### 2. Deploy feature-flags-demo on SAP Cloud Platform

    $ cf api https://api.cf.eu10.hana.ondemand.com
    $ cf login
    $ cf push

### 3. Create a Service Instance of Feature Flags service

#### 3.1 Ensure the `feature-flags` Service Exists in the Marketplace

    $ cf marketplace
    
    -----    
    Getting services from marketplace in org <ORG_ID> / space dev as <USER_ID>...
    OK
    service          	plans    	description
    ...
    feature-flags    	lite     	Feature Flags service for controlling feature rollout
    ...
    

#### 3.2 Create a Service Instance of Feature Flags with Plan `lite`

    $ cf create-service feature-flags lite feature-flags-instance
    
    -----
    Creating service instance feature-flags-instance in org <ORG_ID> / space dev as <USER_ID>...
    OK
    
> **Note:** Alternatively, you can also use the SAP Cloud Platform Cockpit. See [Create a Service Instance](https://help.sap.com/viewer/2250efa12769480299a1acd282b615cf/Cloud/en-US/c7b30b5bf54149148d2302617917dc3e.html).


### 4. Call the feature-flags-demo Application /vcap_services End-Point

> **Note**: Expect to receive an empty JSON.

The /vcap_services end-point simply returns the content of  _VCAP_SERVICES_ environment variable. As for now there is no service instances bound to `feature-flags-demo`, so you will receive an empty JSON.

In the command you use the following URL: \<application_URL\>/vcap_services. You can find the \<application_URL\> in the SAP Cloud Platform Cockpit, in the _feature-flag-demo > Overview > Application Routes_.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services

### 5. Bind feature-flags-demo to feature-flags-instance

    $ cf bind-service feature-flags-demo feature-flags-instance
    
    -----
    Binding service feature-flags-instance to app feature-flags-demo in org <ORG_ID> / space dev as <USER_ID>...
    OK
    TIP: Use 'cf restage feature-flags-demo' to ensure your env variable changes take effect

> **Note:** Alternatively, you can also use the SAP Cloud Platform Cockpit. See [Bind Your Application to the Feature Flags Service Instance](https://help.sap.com/viewer/2250efa12769480299a1acd282b615cf/Cloud/en-US/e7ef0ce6d4b14ae387de5bb18549c250.html).

### 6. Restage feature-flags-demo

Restage `feature-flags-demo` application so the changes in the application environment take effect.

    $ cf restage feature-flags-demo

### 7. Ensure that feature-flags-instance is Bound to feature-flags-demo

> **Note**: Expect to receive the injected environment variables by the Feature Flags service.

    $ curl https://feature-flags-demo.cfapps.eu10.hana.ondemand.com/vcap_services


Sample JSON response:
```json
{
  "feature-flags": [
    {
      "credentials": {
        "password": "aa_GgZf1GIDZbuXV9s0RknzRE+qs0e=",
        "uri": "https://feature-flags.cfapps.eu10.hana.ondemand.com",
        "username": "sbss_x324osjl//pmabsuskr6nshmb2arw6dld4hfb3cj4m2bonkqmm3ts6c68mdpzxz2fma="
      },
      "syslog_drain_url": null,
      "volume_mounts": [ ],
      "label": "feature-flags",
      "provider": null,
      "plan": "lite",
      "name": "feature-flags-instance",
      "tags": [
        "feature-flags"
      ]
    }
  ]
}
```

### 8. Evaluate if the Feature Flag is Missing

> **Note**: Expect the feature flag to be missing.

1. Navigate to feature-flags-demo overview in the SAP Cloud Platform Cockpit (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com). An _Evaluation Form_ opens.
2. Evaluate a feature flag with random name (for example, type in 'my-flag').
The result should state that the feature flag with the given name is missing.

### 9. Create a New Feature Flag

1. Navigate to Feature Flags service instance dashboard in the SAP Cloud Platform Cockpit (for example, https://feature-flags-dashboard.cfapps.eu10.hana.ondemand.com/manageinstances/<instance-id\>). The instance ID is a unique ID of the service instance. 

> **Note**: The easiest way to access the Feature Flags dashboard is through the cockpit. Go to _<your_subaccount> > <your_space> > Service Instances > Actions (from your service instance line) > Open Dashboard icon_.

2. Choose _New Flag_.
3. Fill in the required fields (for example, 'my-flag' for _Name_, 'Super cool feature' for _Description_ and 'OFF' for _State_).
4. Choose _Add_.

### 10. Evaluate the Newly Created Feature Flag

> **Note**: Expect the feature flag to be disabled.

1. Navigate to feature-flags-demo application overview in the SAP Cloud Platform Cockpit. Open the link from the _Application Routes_ section (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com). An _Evaluation Form_ opens.
2. Enter the feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the newly created feature flag.
The result should state that the feature flag with the given name is disabled.

### 11. Enable the Feature Flag

1. Navigate to Feature Flags service instance dashboard in the SAP Cloud Platform Cockpit (for example, https://feature-flags-dashboard.cfapps.eu10.hana.ondemand.com/manageinstances/<instance-id\>). The instance ID is a unique ID of the service instance. 

> **Note**: The easiest way to access the Feature Flags dashboard is through the cockpit. Go to _<your_subaccount> > <your_space> > Service Instances > Actions (from your service instance line) > Open Dashboard icon_.

2. Enable the feature flag using the switch in the _State_ column.

### 12. Verify that the Feature Flag is Enabled

> **Note**: Expect the feature flag to be enabled.

1. Navigate to feature-flags-demo application overview in the SAP Cloud Platform Cockpit. Open the link from the _Application Routes_ section (for example, https://feature-flags-demo.cfapps.eu10.hana.ondemand.com). An _Evaluation Form_ opens.
2. Enter the feature flag name in the _Feature Flag Name_ field and choose _Evaluate_.
3. Evaluate the feature flag.
The result should state that the feature flag with the given name is enabled.
