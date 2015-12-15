# FIX
* En el response cuando no hay datos en los reports hacer un redirect a no se encuentra la informacion o algo similar
* Cargar los controladores de forma selectiva para cada pagina (includes de javascript)
* Arreglar el funcionamiento del serializador cuando se utiliza eclipselink-weaving

# TODO
* Añadir @Version a las entidades para controlar cambios concurrentes
* Desglose ajustes manuales en el PDF de liquidacion
* Dos flags a nivel de operadora para enviar liquidaciones o miniliquidaciones
* Hacer el PDF de miniliquidaciones
* Añadir el centro de coste a los informes de liquidaciones
* Formato separador miles formateo BigDecimal
* Gestion de usuarios y permisos
* Filtro de summary report por centro de coste (el problema es que esta asociado a los locales, no a los operadores)
* Revisar PDF con el desglose de ajustes en las miniliquidaciones
* Revisar excel el auto-size de las columnas
* El isReadOnly esta definido a nivel de $rootScope. Hacer que sea a nivel de scope
* Quitar los mensajes de javascript y recuperarlos a traves del filter translate
* Meter como parte de la build la fecha de generación (lo estoy haciendo ahora manualmente)
* Añadir los RegisterActivity que faltan
* Historico en la relacion de terminales y locales
* Revisión seguridad descargas (no usan ahora el sessionid y tienen el @PermitAll)
* Sacar named queries a orm.xml (quedan 23 por migrar)
* No generar el PDF de las facturas que están a cero y eliminar las que estan generadas
* Cleanup: remove unused bval(js-303) components
	