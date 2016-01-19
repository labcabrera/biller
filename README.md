# FIX 2016-01-19
* No aplicar en el modelo SAT < 0
* Informe liquidaciones: cambios margen -> saldo de caja (done)
* Informe resument liquidaciones: error ajustes internos / externos (done)
* Ajustes miniliquidaciones en report de ajustes (done)
* I18n envio correos (done)
* I18n tipos local (done)
* Posibilidad de cambiar el IBAN (done)
* Corregir NaN cuando se aplica el filtro de angular | abs (done)
* Envio de facturas / miniliquidaciones. Separar funcionamiento y esconder el boton cuando la factura este a cero
* Desglose ajustes en el PDF de la liquidación

# FIX
* Arreglar el funcionamiento del serializador cuando se utiliza eclipselink-weaving (esta deshabilitado de momento).
* Serializacion en los listados de ajustes manuales

# TODO
* Filtro por grupo y centro de coste en el report de liquidaciones

# MINOR
* Añadir @Version a las entidades para controlar cambios concurrentes (solo esta para la jerarquia de LegalEntity).
* Desglose ajustes manuales en el PDF de liquidacion (pendiente de que hacer).
* Añadir el centro de coste al filtro de informes de liquidaciones.
* Revisar PDF con el desglose de ajustes en las miniliquidaciones.
* El isReadOnly esta definido a nivel de $rootScope. Hacer que sea a nivel de scope.
* Quitar los mensajes de javascript y recuperarlos a traves del filter translate.
* Meter como parte de la build la fecha de generación (lo estoy haciendo ahora manualmente).
* Añadir los RegisterActivity que faltan.
* Historico en la relacion de terminales y locales.
* Revisión seguridad descargas (no usan ahora el sessionid y tienen el @PermitAll).
* Sacar named queries a orm.xml (quedan 23 por migrar).
* Cleanup: remove unused bval(js-303) components.
* En el response cuando no hay datos en los reports hacer un redirect a no se encuentra la informacion o algo similar.
* Cargar los controladores de forma selectiva para cada pagina (includes de javascript).

# CHECK-LIST
* Dos flags a nivel de operadora para enviar liquidaciones o miniliquidaciones.
* Terminal PDF de miniliquidaciones.
* Gestion de usuarios y permisos.
* Revisar excel el auto-size de las columnas.
* No generar el PDF de las facturas que están a cero y eliminar las que estan generadas.
