# FIX
* Componente para campos tipo fecha
* Revisar apartado de modificacion del rappel del modelo (esta provocando un error)
* En el response cuando no hay datos en los reports hacer un redirect a no se encuentra la informacion o algo similar
* En pre no aparece el usuario en el listado de actividad
* Cargar los controladores de forma selectiva para cada pagina

# TODO
* Report de resumen de liquidaciones
* Revisar funcionamiento de la generacion de numeros de facturas
* Unificar mensajes de alerta. Quitar los $scope.displayAlert(data);
* El isReadOnly esta definido a nivel de $rootScope. Hacer que sea a nivel de scope
* Eliminar los "window.confirm(..."
* Quitar los mensajes de javascript y recuperarlos a traves del filter translate
* Meter como parte de la build la fecha de generación (lo estoy haciendo ahora manualmente)
* Configuracion de logback en produccion / preproducción
* Eliminar el reportFile de la liquidacion
* Añadir los RegisterActivity que faltan
* Historico en la relacion de terminales y locales
* Optimizacion relaciones JPA
* Revisión seguridad descargas (no usan ahora el sessionid y tienen el @PermitAll)
* Sacar named queries a orm.xml (quedan 23 por migrar)
* No generar el PDF de las facturas que están a cero
* Administrador de roles y permisos
* Eliminar los conceptos de ajuste de caja del modelo
* Eliminar la facturas sin resultado
* Cambio en los modelos de facturacion. Tener un modelo para bares y otro diferentes para operadores

# CHECK-LIST
* Revisar reporte de liquidaciones tras cambio de modelo
* Proceso de migración de facturas y liquidaciones
* Optimización serializadores
* Revisar error paginacion facturas (por ejemplo SOLMAR VALENCIA en septiembre)
* Unificar cuadros de mensajes
* Migración de MANUAL_WITH_LIQUIDATION y MANUAL_WITHOUT_LIQUIDATION (2 registros solamente)	
	