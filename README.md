# FIX
* En el response cuando no hay datos en los reports hacer un redirect a no se encuentra la informacion o algo similar
* Cargar los controladores de forma selectiva para cada pagina (includes de javascript)
* Arreglar el funcionamiento del serializador cuando se utiliza eclipselink-weaving 

# TODO
* Filtro de summary report por centro de coste (el problema es que esta asociado a los locales, no a los operadores)
* Formato separador miles en el front
* Revisar PDF con el desglose de ajustes en las miniliquidaciones
* Revisar excel el auto-size de las columnas
* Report de resumen de liquidaciones
* Revisar funcionamiento de la generacion de numeros de facturas
* Unificar mensajes de alerta. Quitar los $scope.displayAlert(data);
* El isReadOnly esta definido a nivel de $rootScope. Hacer que sea a nivel de scope
* Quitar los mensajes de javascript y recuperarlos a traves del filter translate
* Meter como parte de la build la fecha de generación (lo estoy haciendo ahora manualmente)
* Configuracion de logback en produccion / preproducción
* Añadir los RegisterActivity que faltan
* Historico en la relacion de terminales y locales
* Revisión seguridad descargas (no usan ahora el sessionid y tienen el @PermitAll)
* Sacar named queries a orm.xml (quedan 23 por migrar)
* No generar el PDF de las facturas que están a cero y eliminar las que estan generadas
* Administrador de roles y permisos
* Refactor de los modelos de facturacion. Tener un modelo para bares y otro diferentes para operadores
* Revisar apartado de modificacion del rappel del modelo (esta provocando un error)
* Buscador titulares: posibilidad de buscar eliminados


# CHECK-LIST
* Revisar comportamiento fechas y UTC
* Revisar reporte de liquidaciones tras cambio de modelo
* Proceso de migración de facturas y liquidaciones
* Revisar error paginacion facturas (por ejemplo SOLMAR VALENCIA en septiembre)
* Unificar cuadros de mensajes
* Migración de MANUAL_WITH_LIQUIDATION y MANUAL_WITHOUT_LIQUIDATION (2 registros solamente)	
	