# Pagos en Android con Stripe
Pagos con Stripe en Android con Java y API

## Configuración del proyecto
Añadir la siguiente dependencia al Gradle (Module app).
```
dependencies {
  ...
  
  // Stripe Android SDK
  implementation 'com.stripe:stripe-android:20.17.0'
}
```

Cambiar el SDK a 33.
```
android {
  ...
  compileSdk 33
  
  defaultConfig {
    ...
    
    targetSdk 33
  }
}
```

## Programación para el pago
Se encuentra en el archivo `ServiciosActivity.java`.
