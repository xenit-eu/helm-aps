# Xenit APS Helm Chart

[![Latest version of 'activiti' @ Cloudsmith](https://api-prd.cloudsmith.io/v1/badges/version/xenit/open-source/helm/activiti/latest/x/?render=true&show_latest=true)](https://cloudsmith.io/~xenit/repos/open-source/packages/detail/helm/activiti/latest/)

This is a helm chart for installing Alfresco

## Helm

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com). Cloudsmith is the only fully
hosted, cloud-native, universal package management solution, that enables your organization to create, store and share
packages in any format, to any place, with total confidence.

You can install this helm chart on you K8s cluster. Keep in mind that you will need to add some `--set` statements for
this to work:

```bash
helm install activiti \
  --repo 'https://repo.xenit.eu/public/open-source/helm/charts/'
```

Or you can use it as a dependency in your `requirements.yaml` in your own chart.

```yaml
dependencies:
  - name: aps
    version: 0.0.1
    repository: https://repo.xenit.eu/public/open-source/helm/charts/
```

## Dev Requirements

Make sure you have the following installed:

* Kubectl: https://kubernetes.io/docs/tasks/tools/#kubectl
* docker: https://www.docker.com/get-started/
* Helm: https://helm.sh/docs/intro/install/
* kind: https://kind.sigs.k8s.io/docs/user/quick-start/
* skaffold: https://skaffold.dev/docs/install/

## Start Helm chart

* set up the image pull secrets like in the example and add them to the ```general.imagePullSecrets```
  Example :

```
apiVersion: v1
kind: Secret
metadata:
  name: secretName
  namespace: {{ .Release.Namespace | quote }}
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: {{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"auth\":\"%s\"}}}" <<registry>> <<username>> <<password>> (printf "%s:%s" .username .password | b64enc) | b64enc }}
```

* provide license file for aps activiti:

```bash
 kubectl create secret generic activiti-license-secret --from-file=./activiti.li --namespace=$NAMESPACE
```

* wait for the ingress controller to be ready you can check by running this command :
  ```bash
  kubectl wait --namespace ingress-nginx   --for=condition=ready pod   --selector=app.kubernetes.io/component=controller  --timeout=90s
  ```

## Configuration

### General

#### `general.strategy`

* Required: false
* Default:
  ```yaml
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
  ```
* Description: You can overwrite here the rollout strategy of deployments. This will be effective on ALL deployments in
  the helm chart that have strategy type RollingUpdate (default)

#### `general.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to ALL deployments and statefullSets

#### `general.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here. The secrets will be referenced in all Deployments and StatefullSets.

#### `general.serviceType`

* Required: false
* Default: None
* Description: will set a serviceType on the services that are exposed via an ingress. This might be useful for example
  when you are working on AWS infra with an AWS ALB which requires NodePort services

#### `general.dbActiviti.username`

* Required: false
* Default: None
* Description: Used in the activiti pod to access the Database and to set the username of the rootuser of the
  postgresql-activiti (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random user will be used.

#### `general.dbActiviti.password`

* Required: false
* Default: None
* Description: Used in the activiti pod to access the Database and to set the password of the rootuser of the
  postgresql-activiti (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random password will be used.

#### `general.dbActivitiAdmin.username`

* Required: false
* Default: None
* Description: Used in the activiti-admin pod to access the Database and to set the username of the rootuser of the
  postgresql-activiti-admin (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random user will be used.

#### `general.dbActivitiAdmin.password`

* Required: false
* Default: None
* Description: Used in the activiti-admin pod to access the Database and to set the password of the rootuser of the
  postgresql-activiti-admin (if enabled)
* Note: If not specified the helm chart will try to reuse the value used in previous deployments. If these are not there
  a random password will be used.

#### `general.secrets.dbActiviti.selfManaged`

* Required: false
* Default: false
* Please note that when you enable this you are yourself responsible to provide a secret activiti-db-secret in the
  namespace that
  you will install this chart in.
* Secret data expected:

```
  ACTIVITI_DATASOURCE_USERNAME
  ACTIVITI_DATASOURCE_PASSWORD
  POSTGRES_USER
  POSTGRES_PASSWORD
```

#### `general.secrets.dbActivitiAdmin.selfManaged`

* Required: false
* Default: false
* Please note that when you enable this you are yourself responsible to provide a secret activiti-db-secret in the
  namespace that
  you will install this chart in.
* Secret data expected:

```
  ACTIVITI_ADMIN_DATASOURCE_USERNAME
  ACTIVITI_ADMIN_DATASOURCE_PASSWORD
  POSTGRES_USER
  POSTGRES_PASSWORD
```

### Ingress

#### `ingress.host`

* Required: true
* Default: None
* Description: The host that points to the alfresco cluster for all services besides the syncService service

#### `ingress.ingressAnnotations`

* Required: false
* Default:
  ```
  kubernetes.io/ingress.class: "nginx"
  cert-manager.io/cluster-issuer: "letsencrypt-production"
  ```
* Description: Annotations for ingress

#### `ingress.additionalPaths`

* Required: false
* Default: None
* Example:

```yaml
- path: /service-path
  pathType: Prefix
  backend:
    service:
      name: service-name
      port:
        number: service-port
```

* Description: used to add more path to ingress under the same host name for new services

#### `ingress.mapToRootOnly.port`

* Required: false
* Default: true
* Description: used to add defaultBackend to spec of ingress

#### `ingress.defaultBackend.service`

* Required: false
* Default: nginx-default-service
* Description: the default service name that ingress will point to

#### `ingress.defaultBackend.port`

* Required: false
* Default: 30403
* Description: the default service port that ingress will point to

#### `ingress.blockedPaths.enabled`

* Required: false
* Default: `false`
* Description: Enable 403 handler for blocked Paths endpoints

#### `ingress.blockedPaths.paths`

* Required: false
* Example:

```yaml
- /alfresco/s/api/solr
- /alfresco/service/api/solr
- /alfresco/service/api/solr
- /alfresco/wcservice/api/solr
```

* Description: List of paths that are blocked

### activiti

#### `activiti.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `activiti.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `activiti.image.repository`

* Required: false
* Default: `alfresco/process-services`
* Description: The repository of the docker image that will be used

#### `activiti.image.tag`

* Required: false
* Default: `24.2.0`
* Description: The tag of the docker image that will be used

#### `activiti.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `activiti.livenessProbe`

* Required: false
* Default:

```
    failureThreshold: 5
    httpGet:
      path: /activiti-app/app/rest/locale
      port: 8080
      scheme: HTTP
    initialDelaySeconds: 25
    periodSeconds: 10
    successThreshold: 1
    timeoutSeconds: 5
```

* Description: Specify the livenessProbe configuration

#### `activiti.readinessProbe`

* Required: false
* Default:

```
    failureThreshold: 5
    httpGet:
      path: /activiti-app/app/rest/locale
      port: 8080
      scheme: HTTP
    initialDelaySeconds: 25
    periodSeconds: 10
    successThreshold: 1
    timeoutSeconds: 5
```

* Description: Specify the readinessProbe configuration

#### `activiti.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `activiti.additionalEnvironmentVariables`

* Required: false
* Default:
  ``` yaml
  ACTIVITI_DATASOURCE_DRIVER: "org.postgresql.Driver"
  ACTIVITI_DATASOURCE_URL: "jdbc:postgresql://postgresql-activiti-service:5432/activiti?characterEncoding=UTF-8"
  ACTIVITI_HIBERNATE_DIALECT: "org.hibernate.dialect.PostgreSQLDialect"
  ```
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `activiti.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the activiti-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
- secretRef:
    name: es-secret
```

#### `activiti.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the activiti deployment

#### `activiti.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the activiti service

#### `activiti.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `activiti.resources`

* Required: false
* Default:
  ```yaml
  resources:
    limits:
      memory: 8Gi
    requests:
      cpu: 500m
      memory: 8Gi
  ```
* Description: The resources a node requires

#### `activiti.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

#### `activiti.additionalVolumeMounts`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the activiti pods.
  Make sure the configMap specified exists. Layout should be as follows:

```yaml
      - mountPath: >-
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad1	
        name: ldap1-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad2	
        name: ldap2-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad3	
        name: ldap3-ad-auth-volume	
        readOnly: true
```

#### `activiti.additionalVolumes`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the alfresco pods. Make sure the configMap
  specified exists. Layout should be as follows:

```yaml
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap1-ad-auth-config
        name: ldap1-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap2-ad-auth-config
        name: ldap2-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap3-ad-auth-config
        name: ldap3-ad-auth-volume
```
#### `activiti.license.enabled`

* Required: false
* Default: `false`
* Description: enabling license mounting

#### `activiti.license.path`

* Required: false
* Default: `/home/alfresco/.activiti/enterprise-license/activiti.lic`
* Description: the path to license file where the activiti-license-secret will be put at , to be picked up by
  activiti on startup

### activiti-admin

#### `activitiAdmin.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `activitiAdmin.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `activitiAdmin.image.repository`

* Required: false
* Default: `alfresco/process-services-admin`
* Description: The repository of the docker image that will be used

#### `activitiAdmin.image.tag`

* Required: false
* Default: `24.2.0`
* Description: The tag of the docker image that will be used

#### `activitiAdmin.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `activitiAdmin.livenessProbe`

* Required: false
* Default:

```
    failureThreshold: 5
    httpGet:
      path: /activiti-admin/
      port: 8080
      scheme: HTTP
    initialDelaySeconds: 25
    periodSeconds: 10
    successThreshold: 1
    timeoutSeconds: 5
```

* Description: Specify the livenessProbe configuration

#### `activitiAdmin.readinessProbe`

* Required: false
* Default:

```
    failureThreshold: 5
    httpGet:
      path: /activiti-admin/
      port: 8080
      scheme: HTTP
    initialDelaySeconds: 25
    periodSeconds: 10
    successThreshold: 1
    timeoutSeconds: 5
```

* Description: Specify the readinessProbe configuration

#### `activitiAdmin.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `activitiAdmin.additionalEnvironmentVariables`

* Required: false
* Default:
  ``` yaml
  ACTIVITI_ADMIN_DATASOURCE_DRIVER: "org.postgresql.Driver"
  ACTIVITI_ADMIN_DATASOURCE_URL: "jdbc:postgresql://postgresql-activiti-admin-service:5432/activiti-admin?characterEncoding=UTF-8"
  ACTIVITI_ADMIN_HIBERNATE_DIALECT: "org.hibernate.dialect.PostgreSQLDialect"
  ACTIVITI_ADMIN_REST_APP_HOST: 'http://activiti-service'
  ACTIVITI_ADMIN_REST_APP_PORT: '30000'
  ```
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `activitiAdmin.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the activiti-admin-container envFrom section. This was added to allow to
  integrate secrets
  that are not added by this helm chart.
* Example:

```yaml
- secretRef:
    name: es-secret
```

#### `activitiAdmin.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the activiti-admin deployment

#### `activitiAdmin.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the activiti-admin service

#### `activitiAdmin.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `activitiAdmin.resources`

* Required: false
    * Default:
  ```yaml
  resources:
    limits:
      memory: 2Gi
    requests:
      cpu: 250m
      memory: 2Gi
  ```
* Description: The resources a node requires

#### `activitiAdmin.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

#### `activitiAdmin.additionalVolumeMounts`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the activiti pods.
  Make sure the configMap specified exists. Layout should be as follows:

```yaml
      - mountPath: >-
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad1	
        name: ldap1-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad2	
        name: ldap2-ad-auth-volume	
        readOnly: true	
      - mountPath: >-	
          /usr/local/tomcat/shared/classes/alfresco/extension/subsystems/Authentication/ldap-ad/oup-ad3	
        name: ldap3-ad-auth-volume	
        readOnly: true
```

#### `activitiAdmin.additionalVolumes`

* Required: false
* Default: None
* Description: A list of configMaps that need to be mounted as volumes to the alfresco pods. Make sure the configMap
  specified exists. Layout should be as follows:

```yaml
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap1-ad-auth-config
        name: ldap1-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap2-ad-auth-config
        name: ldap2-ad-auth-volume
      - configMap:
          defaultMode: 420
          items:
            - key: ldap-ad-authentication.properties
              path: ldap-ad-authentication.properties
          name: ldap3-ad-auth-config
        name: ldap3-ad-auth-volume
```

### Activiti Postgresql

#### `postgresqlActiviti.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the PostgresQl

#### `postgresqlActiviti.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `postgresqlActiviti.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `postgresqlActiviti.image.repository`

* Required: false
* Default: `xenit/postgres`
* Description: The repository of the docker image that will be used

#### `postgresqlActiviti.image.tag`

* Required: false
* Default: `latest`
* Description: The tag of the docker image that will be used

#### `postgresqlActiviti.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `postgresqlActiviti.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `postgresqlActiviti.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `postgresqlActiviti.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the postgresql-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `postgresqlActiviti.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl deployment

#### `postgresqlActiviti.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl service

#### `postgresqlActiviti.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `postgresqlActiviti.resources`

* Required: false
* Default:
  ```yaml
  resources:
    requests:
      memory: "1Gi"
      cpu: "1"
  ```
* Description: The resources a node should keep reserved for your pod

#### `postgresqlActiviti.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Activiti Admin Postgresql

#### `postgresqlActivitiAdmin.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the PostgresQl

#### `postgresqlActivitiAdmin.replicas`

* Required: false
* Default: `1`
* Description: The number of pods that will be running

#### `postgresqlActivitiAdmin.image.registry`

* Required: false
* Default: `docker.io`
* Description: The registry where the docker container can be found in

#### `postgresqlActivitiAdmin.image.repository`

* Required: false
* Default: `xenit/postgres`
* Description: The repository of the docker image that will be used

#### `postgresqlActivitiAdmin.image.tag`

* Required: false
* Default: `latest`
* Description: The tag of the docker image that will be used

#### `postgresqlActivitiAdmin.imagePullPolicy`

* Required: false
* Default: `IfNotPresent`
* Description: Specify when the pods should pull the image from the repositories

#### `postgresqlActivitiAdmin.strategy.type`

* Required: false
* Default: `RollingUpdate`
* Description: Can be set to `Recreate` if you want all your pods to be killed before new ones are created

#### `postgresqlActivitiAdmin.additionalEnvironmentVariables`

* Required: false
* Default: None
* Example:
  ```yaml
  environmentVariable1Key: environmentVariable1Value
  environmentVariable2Key: environmentVariable2Value
  ```
* Description: With this list of parameters you can add 1 or multiple environment variables that will be passed to the
  docker container. These will be stored in a config and are hence not safe for sensitive information

#### `postgresqlActivitiAdmin.envFrom`

* Required: false
* Default: None
* Description: This allows you to add to the postgresql-container envFrom section. This was added to allow to integrate
  secrets
  that are not added by this helm chart.
* Example:

```yaml
  - secretRef:
    name: s3-secret
```

#### `postgresqlActivitiAdmin.podAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl deployment

#### `postgresqlActivitiAdmin.serviceAnnotations`

* Required: false
* Default: None
* Example:
  ```yaml
  annotation1Key: annotation1Value
  annotation2Key: annotation2Value
  ```
* Description: With this list of parameters you can add 1 or multiple annotations to the PostgresQl service

#### `postgresqlActivitiAdmin.serviceAccount`

* Required: false
* Default: None
* Description: If your pods need to run with a service account you can specify that here. Please note that you are
  yourself responsible to create the serviceAccount referenced in the namespace of this helm chart

#### `postgresqlActivitiAdmin.resources`

* Required: false
* Default:
  ```yaml
  resources:
    requests:
      memory: "1Gi"
      cpu: "1"
  ```
* Description: The resources a node should keep reserved for your pod

#### `postgresqlActivitiAdmin.imagePullSecrets`

* Required: false
* Default: None
* Example:
  ```yaml
    - name: privateDockerRepo1Secret
    - name: privateDockerRepo2Secret
  ```
* Description: If you use an image that is not public. then you can create dockerconfigjson secrets on your cluster and
  reference them here.

### Persistent Storage

### postgresql Activiti

#### `persistentStorage.postgresqlActiviti.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the PostgresQL Activiti pods

#### `persistentStorage.postgresqlActiviti.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.postgresqlActiviti.storage`

* Required: false
* Default: `2`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.postgresqlActiviti.efs.volumeHandle`

* Required: when `persistentStorage.postgresqlActiviti.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location

### postgresql Activiti Admin

#### `persistentStorage.postgresqlActivitiAdmin.enabled`

* Required: false
* Default: `true`
* Description: Enable or disable the creation of a PV and PVC for the PostgresQL Activiti Admin pods

#### `persistentStorage.postgresqlActivitiAdmin.storageClassName`

* Required: false
* Default: `scw-bssd`
* Description: Provide what storageClass should be used. For values other then `scw-bssd` `standard`
  or `efs-storage-class` you will need to make sure that that storage class is created

#### `persistentStorage.postgresqlActivitiAdmin.storage`

* Required: false
* Default: `2`
* Description: The size in GB of the volume that should be reserved

#### `persistentStorage.postgresqlActivitiAdmin.efs.volumeHandle`

* Required: when `persistentStorage.postgresqlActivitiAdmin.storageClassName` is `scw-bssd`
* Default: None
* Description: The volume handle pointing to the AWS EFS location
