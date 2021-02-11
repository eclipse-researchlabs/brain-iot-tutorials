# Admin User Interface

Provides a view of Smart Behaviours in marketplace and allows them to be installed/uninstalled.

## Implementation

The Brain-IoT Admin UI provides a REST server for an embedded Javascript UI by extending the Paremus UI server: https://github.com/paremus/ui_server.

It implements REST endpoints that link into the event-bus for:

* Behaviours
* Events
* Fabrics
* Hosts

The Javascript UI is unchanged from https://github.com/paremus/js_client.

## Testing

The app.test sub-project allows stand-alone testing of the UI. To launch standalone UI:

```
bnd run app.test/app.bndrun
```

Now browse to http://localhost:8082/config and login as admin (password: admin)

Then select the Behaviour Management Service and and set the Marketplace indexes, for example to: [security-light-marketplace](https://nexus.repository-pert.ismb.it/repository/marketplaces/com.paremus.brain.iot.marketplace/security-light-marketplace/0.0.1-SNAPSHOT/index.xml)

## end