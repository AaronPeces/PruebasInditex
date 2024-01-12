/***** MAIN *****/

void actualizarRango(Map ctx) {
    def fechaModificacion = ctx.fechaModificacion
    def bimestreModificacion = ctx.bimestreModificacion
    def rangoInicial = ctx.rangoInicial
    def rangoFin = ctx.rangoFin

    def anhoActual = Calendar.getInstance().get(Calendar.YEAR)
    def bimestreActual = ((Calendar.getInstance().get(Calendar.MONTH) / 2) + 1).intValue()
    def anhoModificacion = Integer.parseInt(fechaModificacion.split("-")[0])
    def bimestreModificacion = bimestreModificacion


    //Validación de los datos:

    if (!validarFecha(fechaModificacion)) {
        info "Fecha de modificacion no válida"
        return
    }

    if (!validarRango(rangoInicial, rangoFin)) {
        info "El rango no es válido"
        return
    }

    if(!validarPeriodo(anhoModificacion,anhoActual,bimestresModificacion,bimestreActual)){
        info "Solo se puede actualizar o insertar un periodo futuro."
        return
    }


    //Busqueda, actualizacion o creacion del registro:

    def registro = buscarRegistro(rangoInicial)

    if (registro) {
        actualizarRegistro(registro, fechaModificacion, bimestresModificacion, rangoInicial, rangoFin)
    } else {
        insertarRegistro(fechaModificacion, bimestresModificacion, rangoInicial, rangoFin)
    }
}




/***** Metodos de validacion *****/

boolean validarFecha(String fecha) {
    try {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(fecha)
        return true
    } catch (Exception e) {
        return false
    }
}

boolean validarRango(String rangoInicial, String rangoFin) {
    def diff = Long.parseLong(rangoFin.substring(3)) - Long.parseLong(rangoInicial.substring(3))
    return diff >= 1000
}

boolean validarPeriodo(int anhoModificacion, int anhoActual, int bimestreModificacion, int bimestreActual){
    if (anhoModificacion < anhoActual || (anhoModificacion == anhoActual && bimestreModificacion <= bimestreActual)) {
        return false
    }else{
        return true
    }
}



/***** Metodos interaccion con bbdd *****/

Map buscarRegistro(String rangoInicial) {
    def query = "SELECT * FROM RANGOS WHERE RANGO_INICIAL = :RANGO_INICIAL"
    def params = [:]
    params.RANGO_INICIAL = rangoInicial
    def resultado = sqlQuery(bbdd, query, params)

    if (resultado.size() > 0) {
        return resultado[0]
    } else {
        return null
    }
}

void actualizarRegistro(Map registro, String fechaModificacion, int bimestresModificacion, String rangoInicial, String rangoFin) {
    def query = "UPDATE RANGOS SET AÑO = :AÑO, BIMESTRE = :BIMESTRE, RANGO_INICIAL = :RANGO_INICIAL, RANGO_FINAL = :RANGO_FINAL, ULTIMA_ACTUALIZACION = :ULTIMA_ACTUALIZACION WHERE ID_RANGO = :ID_RANGO"
    def params = [:]
    params.AÑO = Integer.parseInt(fechaModificacion.substring(0, 4))
    params.BIMESTRE = bimestresModificacion
    params.RANGO_INICIAL = rangoInicial
    params.RANGO_FINAL = rangoFin
    params.ULTIMA_ACTUALIZACION = Timestamp.valueOf(fechaModificacion)
    params.ID_RANGO = registro.ID_RANGO
    sqlUpdate(bbdd, query, params)
}

void insertarRegistro(String fechaModificacion, int bimestresModificacion, String rangoInicial, String rangoFin) {
    def query = "INSERT INTO RANGOS (AÑO, BIMESTRE, RANGO_INICIAL, RANGO_FINAL, ULTIMA_ACTUALIZACION) VALUES (:AÑO, :BIMESTRE, :RANGO_INICIAL, :RANGO_FINAL, :ULTIMA_ACTUALIZACION)"
    def params = [:]
    params.AÑO = Integer.parseInt(fechaModificacion.substring(0, 4))
    params.BIMESTRE = bimestresModificacion
    params.RANGO_INICIAL = rangoInicial
    params.RANGO_FINAL = rangoFin
    params.ULTIMA_ACTUALIZACION = Timestamp.valueOf(fechaModificacion)
    sqlUpdate(bbdd, query, params)
}