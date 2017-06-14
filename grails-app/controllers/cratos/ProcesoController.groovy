package cratos

import cratos.seguridad.Persona
import cratos.sri.Pais
import cratos.sri.PorcentajeIva
import cratos.sri.SustentoTributario
import cratos.sri.TipoCmprSustento
import cratos.sri.TipoCmprTransaccion
import cratos.sri.TipoComprobanteSri
import cratos.sri.TipoTransaccion

import java.sql.Connection

class ProcesoController extends cratos.seguridad.Shield {

    def buscadorService
    def kerberosoldService
    def procesoService
    def loginService
    def utilitarioService
    def dbConnectionService

    /* todo acabar los dominios */


    def index = { redirect(action: "lsta") }

    def lsta = {
        def campos = ["estado": ["Estado", "string"], "descripcion": ["Descripción", "string"], "fecha": ["Fecha", "date"], "comp": ["Comprobante", "string"]]
        [campos: campos]
    }

    def nuevoProceso = {
//        println "nuevo proceso "+params
        def tiposProceso = ["-1": "Seleccione...",
                            "C": "C-Compras (Compras Inventario, Compras Gasto)",
                            "V": "V-Ventas (Ventas, Reposición de Gasto)",
                            "A": "A-Ajustes (Diarios y Otros)",
                            "P": "P-Pagos a proveedores",
                            "N": "N-Nota de Crédito"]

        def empresa = Empresa.get(session.empresa.id)
        def libreta = DocumentoEmpresa.findAllByEmpresaAndFechaInicioLessThanEqualsAndFechaFinGreaterThanEqualsAndTipo(empresa, new Date(), new Date(),'F')

        if (params.id) {
            def proceso = Proceso.get(params.id)
            def registro = (Comprobante.findAllByProceso(proceso)?.size() == 0) ? false : true
            def fps = ProcesoFormaDePago.findAllByProceso(proceso)

            render(view: "procesoForm", model: [proceso: proceso, registro: registro, tiposProceso: tiposProceso, fps: fps, libreta: libreta])
        } else
            render(view: "procesoForm", model: [registro: false, tiposProceso: tiposProceso, libreta: libreta])
    }

    def save = {
        println "save proceso: $params"
        def proceso
        def comprobante

        def tptr
        switch (params.tipoProceso) {
            case 'C':
                tptr = 1
                break
            case 'V':
                tptr = 2
                break
        }

        def tipoTran
        if(tptr) {
            tipoTran = TipoTransaccion.get(tptr)  //????
        }

        def sustento = SustentoTributario.get(params.tipoCmprSustento)  //????
        def comprobanteSri = TipoCmprSustento.get(params."tipoComprobanteSri.id")

        def proveedor = Proveedor.get(params."proveedor.id")
        def gestor = Gestor.get(params."gestor.id")
        def fechaRegistro = new Date().parse("dd-MM-yyyy", params.fecha_input)   //fecha del cmpr
        def fechaIngresoSistema = new Date().parse("dd-MM-yyyy",params.fecharegistro_input)   //registro

        if(params.id){
            proceso = Proceso.get(params.id)
            params.tipoProceso = proceso.tipoProceso
        } else {
            proceso = new Proceso()
            proceso.estado = "N"
            proceso.gestor = gestor
            proceso.contabilidad = Contabilidad.get(session.contabilidad.id)
            proceso.empresa = Empresa.get(session.empresa.id)
//            proceso.tipoTransaccion = tipoTran
        }

        proceso.proveedor = proveedor
        println "...1"
        proceso.tipoCmprSustento = comprobanteSri
        proceso.sustentoTributario = sustento

        proceso.fechaRegistro = fechaRegistro
        proceso.fechaIngresoSistema = fechaIngresoSistema
        proceso.descripcion = params.descripcion
        proceso.fecha = new Date()
        proceso.tipoProceso = params.tipoProceso
        println "...2"

        if(params.tipoProceso == 'C') {
            proceso.valor = params.baseImponibleIva0.toDouble() + params.baseImponibleIva.toDouble() +
                    params.baseImponibleNoIva.toDouble()
            proceso.impuesto = params.ivaGenerado.toDouble() + params.iceGenerado.toDouble()
            proceso.baseImponibleIva = params.baseImponibleIva.toDouble()
            proceso.baseImponibleIva0 = params.baseImponibleIva0.toDouble()
            proceso.baseImponibleNoIva = params.baseImponibleNoIva.toDouble()
            proceso.ivaGenerado = params.ivaGenerado.toDouble()
            proceso.iceGenerado = params.iceGenerado.toDouble()
            proceso.documento = params.facturaEstablecimiento + "-" + params.facturaPuntoEmision + "-" + params.facturaSecuencial
            println "documento: ${proceso.factura}"
            proceso.facturaEstablecimiento = params.facturaEstablecimiento
            proceso.facturaPuntoEmision = params.facturaPuntoEmision
            proceso.facturaSecuencial = params.facturaSecuencial
            proceso.facturaAutorizacion = params.facturaAutorizacion
            println "...4"
        }

//        println "<<<<< gestor: ${proceso.gestor?.id}, cont: ${proceso.contabilidad?.id}, empr: ${proceso.empresa?.id}, " +
//                "proveedor: ${proceso.proveedor?.id}, cmpr: ${proceso.comprobante?.id}, usro: ${proceso.usuario}"
//        println "tptr: ${proceso.tipoTransaccion}, tpss: ${proceso.tipoCmprSustento}, tpcp: ${proceso.sustentoTributario}"
//        println "tpps: ${proceso.tipoProceso}, fcha: ${proceso.fecha}, fcig: ${proceso.fechaIngresoSistema}"
//        println "fcrg: ${proceso.fechaRegistro}, fcem: ${proceso.fechaEmision}"
        try {
            println "...5: $proceso"
            proceso.save(flush: true)
            println "...6"
            proceso.refresh()
            redirect(action: 'nuevoProceso', id: proceso.id)
            println "...7 proceso: ${proceso.id}"
        } catch (e) {
            println "...8"
            println "error al grabar el proceso $e"
        }

    }


    def registrar = {
        if (request.method == 'POST') {
            println "registrar " + params
            def pro = Proceso.get(params.id)
            if (pro.estado == "R") {
                render("El proceso ya ha sido registrado previamente")
            } else {
//                def lista = procesoService.registrar(pro, session.perfil, session.usuario, session.contabilidad)
                def lista = procesoService.registrar(pro)
                if (lista[0] != false) {
                    render(view: "detalleProceso", model: [comprobantes: lista[1], asientos: lista[2], proceso: pro])

                } else {
                    render("Error registrando el proceso")
                }

            }
        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }

    def cargaComprobantes = {
//        println "cargar comprobantes " + params
        def proceso = Proceso.get(params.proceso)
        def comprobantes = Comprobante.findByProceso(proceso)
        def asientos = []
        if (comprobantes)
            asientos += Asiento.findAllByComprobante(comprobantes)
        if (asientos.size() > 0) {
            asientos.sort { it.cuenta.numero }
        }
        def aux = false
        asientos.each {
            if (Auxiliar.findAllByAsiento(it).size() > 1)
                aux = true
        }
        //println "comp "+comprobantes+" as "+asientos
        render(view: "detalleProceso", model: [comprobantes: comprobantes, asientos: asientos, proceso: proceso, aux: aux])
    }

    def cargaTcsr() {
        println "cargatcsr $params"
        def cn = dbConnectionService.getConnection()
        def tipo = 0
        switch (params.tptr) {
            case 'C':
                tipo = 1
                break
            case 'V':
                tipo = 2
                break
            default:
                tipo = 0
        }
        def sql = "select cast(tittcdgo as integer) cdgo from titt, prve, tptr " +
                "where prve.tpid__id = titt.tpid__id and prve__id = ${params.prve} and " +
                "tptr.tptr__id = titt.tptr__id and tptrcdgo = '${tipo}'"
        println "sql: $sql"
        def titt = cn.rows(sql.toString())[0]?.cdgo
        println "identif: $titt"
        sql = "select tcst__id id, tcsrcdgo codigo, tcsrdscr descripcion from tcst, tcsr " +
                "where tcsr.tcsr__id = tcst.tcsr__id and titt @> '{${titt}}' and " +
                "sstr @> '{${params.sstr}}' order by tcsrcdgo"

        def data = cn.rows(sql.toString())
        cn.close()
        [data: data, tpcpSri: params.tpcp]
    }

    def cargaSstr() {
        println "cargaSstr $params"
        def cn = dbConnectionService.getConnection()
        def tipo = 0
        switch (params.tptr) {
            case 'C':
                tipo = 1
                break
            case 'V':
                tipo = 2
                break
            default:
                tipo = 0
        }

        def sql = "select cast(tittcdgo as integer) cdgo from titt, prve, tptr " +
                "where prve.tpid__id = titt.tpid__id and prve__id = ${params.prve} and " +
                "tptr.tptr__id = titt.tptr__id and tptrcdgo = '${tipo}'"
        println "sql: $sql"

        def titt = cn.rows(sql.toString())[0]?.cdgo
        println "identif: $titt"

        sql = "select sstr__id id, sstrcdgo codigo, sstrdscr descripcion from sstr " +
                "where sstr__id in (select distinct(unnest(sstr)) " +
                "from tcst where titt @> '{${titt}}') order by 1;"

        def data = cn.rows(sql.toString())
        cn.close()
        [data: data, sstr: params.sstr, tpcpSri: params.tpcp]
    }

    def valorAsiento = {
        if (request.method == 'POST') {
            params.lang="en"
            def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
            def localeResolver = request.getAttribute(key)
            localeResolver.setLocale(request, response, new Locale("en"))
            println "cambiar Valor Asiento " + params
            def vd = params.vd.toDouble()
            def vh = params.vh.toDouble()
            // println "vd "+vd +" vh  "+vh
            def proceso = Proceso.get(params.proceso)
            def comprobantes = Comprobante.findAllByProceso(proceso)
            def asiento = Asiento.get(params.id)
            def asientos = []
            comprobantes.each {
                asientos += Asiento.findAllByComprobante(it)
            }
            params.controllerName = controllerName
            params.actionName = actionName
            asiento.debe = vd
            asiento.haber = vh
            asiento.cuenta = Cuenta.get(params.cnta)
            if (asiento.save(flush: true))
                render "ok"
            else
                render "error"
//            render(view: "detalleProceso", model: [comprobantes: comprobantes, asientos: asientos])
        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }

    /*TODO Crear periodos y probar el mayorizar y desmayorizar... move on*/
    def registrarComprobante = {
        if (request.method == 'POST') {
            println "registrar comprobante " + params
            def comprobante = Comprobante.get(params.id)
            def msn = kerberosoldService.ejecutarProcedure("mayorizar", [comprobante.id, 1])
            println "LOG: mayorizando por comprobante ${comprobante.id}" + msn["mayorizar"]
            try {
                def log = new LogMayorizacion()
                log.usuario = cratos.seguridad.Persona.get(session.usuario.id)
                log.comprobante = comprobante
                log.tipo = "M"
                log.resultado = msn["mayorizar"].toString()
                log.save(flush: true)
            } catch (e) {
                println "LOG: error del login de mayorizar " + msn["mayorizar"].toString()
            }
            if (msn["mayorizar"] =~ "Error") {
                render " " + msn["mayorizar"]
            } else {
                def proceso = comprobante.proceso
                params.controllerName = controllerName
                params.actionName = actionName
                comprobante.registrado = "S"
                comprobante.save(flush: true)
                proceso.estado = "R"
                proceso.save(flush: true)
                render "ok"
            }
        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }

    def desmayorizar() {
        println "demayo " + params
        if (request.method == 'POST') {
            def comprobante = Comprobante.get(params.id)
            def msn = kerberosoldService.ejecutarProcedure("mayorizar", [comprobante.id, -1])
            println "LOG: desmayorizando  comprobante ${comprobante.id} " + msn["mayorizar"]
            try {
                def log = new LogMayorizacion()
                log.usuario = cratos.seguridad.Persona.get(session.usuario.id)
                log.comprobante = comprobante
                log.tipo = "D"
                log.resultado = msn["mayorizar"].toString()
                log.save(flush: true)
            } catch (e) {
                println "LOG: error del login de mayorizar " + msn["mayorizar"].toString()
            }
            if (msn["mayorizar"] =~ "Error") {

                render " " + msn["mayorizar"]
            } else {
                def proceso = comprobante.proceso
                params.controllerName = controllerName
                params.actionName = actionName
//                kerberosService.generarEntradaAuditoria(params,Comprobante,"registrado","R",comprobante.registrado,session.perfil,session.usuario)
                comprobante.registrado = "N"
                comprobante.save(flush: true)
                proceso.estado = "N"
                proceso.save(flush: true)
                render "ok"
            }
        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }

    def listar = {
        //println "buscar proceso"
        def extraComp = ""
        if (params.campos instanceof java.lang.String) {
            if (params.campos == "comp") {
                def comps = Comprobante.findAll("from Comprobante where numero like '%${params.criterios.toUpperCase()}%' or prefijo like '%${params.criterios.toUpperCase()}%' ")
                params.criterios = ""
                comps.eachWithIndex { p, i ->
                    extraComp += "" + p.proceso.id
                    if (i < comps.size() - 1)
                        extraComp += ","
                }
                if (extraComp.size() < 1)
                    extraComp = "-1"
                params.campos = ""
                params.operadores = ""
            }
        } else {
            def remove = []
            params.campos.eachWithIndex { p, i ->
                if (p == "comp") {
                    def comps = Comprobante.findAll("from Comprobante where numero like '%${params.criterios[i].toUpperCase()}%' or prefijo like '%${params.criterios[i].toUpperCase()}%' ")

                    comps.eachWithIndex { c, j ->
                        extraComp += "" + c.proceso.id
                        if (j < comps.size() - 1)
                            extraComp += ","
                    }
                    if (extraComp.size() < 1)
                        extraComp = "-1"
                    remove.add(i)
                }
            }
            remove.each {
                params.criterios[it] = null
                params.campos[it] = null
                params.operadores[it] = null
            }
        }
        def extras = " and empresa=${session.empresa.id} and contabilidad=${session.contabilidad.id}"
        if (extraComp.size() > 1)
            extras += " and id in (${extraComp})"

        def closure = { estado ->
            if (estado == "R")
                return "Registrado"
            else
                return "No registrado"
        }
        def comp = { proceso ->
            def c = Comprobante.findByProceso(proceso)
            if (c)
                return c.prefijo + "" + c.numero
            else
                return ""
        }
        def tipo = { t ->
            switch (t) {
                case "P":
                    return "Pago"
                    break;
                case "C":
                    return "Compra"
                    break;
                case "V":
                    return "Venta"
                    break;
                case "A":
                    return "Ajuste"
                    break;
                case "O":
                    return "Otro"
                    break;
                default:
                    return "Otro"
                    break;
            }
        }
        def listaTitulos = ["Fecha", "Descripcion", "Estado", "Comprobante", "Tipo", "Proveedor"]
        /*Titulos de la tabla*/
        def listaCampos = ["fecha", "descripcion", "estado", "comprobante", "tipoProceso", "proveedor"]
        /*campos que van a mostrarse en la tabla, en el mismo orden que los titulos*/
        def funciones = [["format": ["dd-MM-yyyy "]], null, ["closure": [closure, "?"]], ["closure": [comp, "&"]], ["closure": [tipo, "?"]], null]
        /*funciones para cada campo en caso de ser necesari. Cada campo debe tener un mapa (con el nombre de la funcion como key y los parametros como arreglo) o un null si no tiene funciones... si un parametro es ? sera sustituido por el valor del campo, si es & sera sustituido por el objeto */
        def link = "descripcion"                                      /*nombre del campo que va a llevar el link*/
        def url = g.createLink(action: "listar", controller: "proceso")
        /*link de esta accion ...  sive para la opcion de reporte*/
//        params.ordenado="fecha"
//        params.orden="desc"
        def listaSinFiltro = buscadorService.buscar(Proceso, "Proceso", "excluyente", params, true, extras)
        /* Dominio, nombre del dominio , excluyente dejar asi,params tal cual llegan de la interfaz del buscador, ignore case */
        listaSinFiltro.pop()
        def lista = []
        listaSinFiltro.each {
            if (it.estado != "B")
                lista.add(it.refresh())
        }

        def numRegistros = 10
        if (!params.reporte) {
//            println "no reporte"
            render(view: '../lstaTbla', model: [listaTitulos: listaTitulos, listaCampos: listaCampos, lista: lista, link: link, funciones: funciones, url: url, numRegistros: numRegistros])
        } else {
            /*De esto solo cambiar el dominio, el parametro tabla, el paramtero titulo y el tamaño de las columnas (anchos)*/
//            println "si reporte"
            session.dominio = Proceso
            session.funciones = funciones
            def anchos = [16, 70, 14] /*el ancho de las columnas en porcentajes*/
            redirect(controller: "reportes2", action: "reporteBuscador", params: [listaCampos: listaCampos, listaTitulos: listaTitulos, tabla: "Proceso", orden: params.orden, ordenado: params.ordenado, criterios: params.criterios, operadores: params.operadores, campos: params.campos, titulo: "Transacciones contables", anchos: anchos])
        }

    }

//    def show = {
//        def proceso = Proceso.get(params.id)
//        def tiposProceso = ["-1": "Seleccione...", "C": "Compras", "V": "Ventas", "O": "Otros", "A": "Ajustes", "P": "Pagos", "NC": "Nota de Crédito"]
//        def comprobante = Comprobante.findByProceso(proceso)
//        def registro = (Comprobante.findAllByProceso(proceso)?.size() == 0) ? false : true
//        def fps = ProcesoFormaDePago.findAllByProceso(proceso)
//        def aux = false
//        Asiento.findAllByComprobante(comprobante).each {
//            if (Auxiliar.findAllByAsiento(it).size() > 1)
//                aux = true
//        }
//        render(view: "procesoForm", model: [proceso: proceso, registro: registro, comprobante: comprobante, tiposProceso: tiposProceso, fps: fps, registro: registro, aux: aux])
//    }

    def comprobarPassword = {
        if (request.method == 'POST') {
            println "comprobar password " + params
            def resp = loginService.autorizaciones(session.usuario, params.atrz)

            render(resp)
        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }

    def cargarAuxiliares = {
        //println "cargar auxiliares "+params
        def msn = null
        def asiento = Asiento.get(params.id);
        def auxiliares = Auxiliar.findAllByAsiento(asiento)
        def formas = [:]
        def fps = ProcesoFormaDePago.findAllByProceso(asiento.comprobante.proceso)
        fps.each {
            formas.put(it.id, it.tipoPago.descripcion)
        }
        formas.put("-1", "NOTA DE DEBITO")
        formas.put("-2", "NOTA DE CREDITO")
//        println "Formas "+formas
        if (auxiliares.size() == 0) {
            msn = "EL asiento no tiene registrado ningun plan de pagos"
        }
        def max = Math.abs(asiento.debe - asiento.haber)
        render(view: "detalleAuxiliares", model: [auxiliares: auxiliares, asiento: asiento, msn: msn, max: max, formas: formas])
    }

    def nuevoAuxiliar = {
        if (request.method == 'POST') {
            params.lang="en"
            def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
            def localeResolver = request.getAttribute(key)
            localeResolver.setLocale(request, response, new Locale("en"))
//            println "nuevo aux "+params
            def msn = null
            if (params.razon == "D")
                params["debe"] = params.valor
            else
                params["haber"] = params.valor
            if (params.proveedor.id == "-1") {
                def proceso = Proceso.get(params.proceso)
                if (proceso.proveedor != null)
                    params["proveedor.id"] = proceso.proveedor.id
            }
            def fecha = params.remove("fechaPago")
            def p = new Auxiliar(params)
            p.fechaPago = new Date().parse("dd-MM-yyyy", fecha)
            p.save(flush: true)
            println "nuevo auxiliar " + params + "  " + p.errors
            if (p.errors.getErrorCount() != 0)
                msn = "Error al insertar el auxiliar revise los datos ingresados"
            else
                redirect(action: "cargarAuxiliares", params: ["id": params.asiento.id])
//      def asiento=p.asientoContable
//      def auxiliares=Auxiliar.findAllByAsientoContable(asiento)

        } else {
            redirect(controller: "shield", action: "ataques")
        }
    }
    def borrarAuxiliar = {

        if (request.method == 'POST') {
            println " borrar auxiliar " + params
            def aux = Auxiliar.get(params.id)
            def pagos = PagoAux.findAllByAuxiliar(aux)
            if (pagos.size() > 0) {
                render "error"
                return
            } else {
                aux.delete(flush: true)
                def asiento = Asiento.get(params.idAs)
                def auxiliares = Auxiliar.findAllByAsiento(asiento)
                render(view: "detalleAuxiliares", model: [auxiliares: auxiliares, asiento: asiento])
            }

        } else {
            redirect(controller: "shield", action: "ataques")
        }

    }

    def buscarProveedor = {
//         println "buscar proveedor "+params
        def provs = []
        def proceso = Proceso.get(params.proceso)
        def tr = TipoRelacion.list()
        def tipo =   " 1,2,3 "
        switch (params.tipoProceso) {
            case "P":
                tr = TipoRelacion.findAllByCodigoInList(['C','P'])
                tipo = " 1, 3 "
                break
            case "NC" :
                tr = TipoRelacion.findAllByCodigoInList(['C','E'])
                tipo = " 2,3 "
                break
            case "V"  :
                tr = TipoRelacion.findAllByCodigoInList(['C','E'])
                tipo = " 2, 3 "
                break
        }

        def cn = dbConnectionService.getConnection()
        def sql

        if(params?.par?.trim() == ""){
            sql = "select prve__id id, prve_ruc ruc, prvenmbr nombre, tppvdscr tipoProveedor from prve, tppv " +
                    "where tppv.tppv__id = prve.tppv__id and tprl__id in (${tipo}) and empr__id = ${session?.empresa?.id} order by prve_ruc;"
        }else{
            if(params.tipo == "1"){
                sql = "select prve__id id, prve_ruc ruc, prvenmbr nombre, tppvdscr tipoProveedor from prve, tppv " +
                        "where tppv.tppv__id = prve.tppv__id and tprl__id in (${tipo}) and empr__id = ${session?.empresa?.id} and prve_ruc like '%${params.par}%' order by prve_ruc;"
            }else{
                sql = "select prve__id id, prve_ruc ruc, prvenmbr nombre, tppvdscr tipoProveedor from prve, tppv " +
                        "where tppv.tppv__id = prve.tppv__id and tprl__id in (${tipo}) and empr__id = ${session?.empresa?.id} and prvenmbr ilike '%${params.par}%' order by prve_ruc;"
            }
        }

        provs = cn.rows(sql.toString())

        [provs: provs]
    }

    def detallePagos = {
        def aux = Auxiliar.get(params.id)
        def pagos = PagoAux.findAllByAuxiliar(aux)
        [pagos: pagos, aux: aux]
    }

    def savePagoNota = {
        println "save pago nota " + params
        params.lang="en"
        def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
        def localeResolver = request.getAttribute(key)
        localeResolver.setLocale(request, response, new Locale("en"))
        def fecha = params.remove("fecha")
        def fechaEmi = new Date().parse("dd-MM-yyyy", params.fechaEmision)
        params.remove("fechaEmision")
        def pago = new PagoAux(params)
        pago.fecha = new Date().parse("dd-MM-yyyy", fecha)
        pago.fechaEmision = fechaEmi
        if (params.tipo == "-1")
            pago.tipo = "D"
        else
            pago.tipo = "C"
        if (pago.save(flush: true)) {
            def proceso = new Proceso()
            proceso.gestor = Gestor.get(params.gestor)
            proceso.contabilidad = session.contabilidad
            proceso.descripcion = "Nota de ${(pago.tipo == 'C') ? 'Crédito' : 'Débito'} " + new Date().format("dd-MM-yyyy hh:mm") + " Monto: " + (pago.monto + pago.impuesto) + " Proveedor: " + pago.auxiliar?.asiento?.comprobante?.proceso?.proveedor
            proceso.estado = "R"
            proceso.fecha = new Date()
            proceso.proveedor = pago.auxiliar.asiento.comprobante.proceso.proveedor
            proceso.tipoPago = pago.auxiliar.asiento.comprobante.proceso.tipoPago
            proceso.usuario = session.usuario
            /*TODO preguntar que es esto?*/
            //proceso.tipoComprobanteId = TipoComprobanteId.get(3)
            proceso.valor = pago.monto
            proceso.impuesto = 0
            proceso.documento = pago.referencia
            proceso.pagoAux = pago
            proceso.tipoProceso = "P"
            proceso.empresa = session.empresa
            println "valor " + proceso.valor + " inp " + proceso.impuesto
            if (proceso.save(flush: true)) {
                procesoService.registrar_old(proceso, session.perfil, session.usuario, session.contabilidad)
                render "ok"
            } else {
                println "error en el proceso " + proceso.errors
                render "error"
            }

        } else {
            render "error"
            println "error save pago nota " + pago.errors
        }
    }

    def savePago = {
        println "save pago " + params
        params.lang="en"
        def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
        def localeResolver = request.getAttribute(key)
        localeResolver.setLocale(request, response, new Locale("en"))
        def fecha = params.remove("fecha")
        def pago = new PagoAux(params)
        pago.fecha = new Date().parse("dd-MM-yyyy", fecha)
        if (pago.save(flush: true)) {
            def proceso = new Proceso()
            proceso.gestor = Gestor.get(params.gestor)
            proceso.contabilidad = session.contabilidad
            proceso.descripcion = "Pago  " + new Date().format("dd-MM-yyyy hh:mm") + " Monto: " + pago.monto + " Proveedor: " + pago.auxiliar?.asiento?.comprobante?.proceso?.proveedor
            proceso.estado = "R"
            proceso.fecha = new Date()
            proceso.proveedor = pago.auxiliar.asiento.comprobante.proceso.proveedor
            proceso.tipoPago = pago.auxiliar.asiento.comprobante.proceso.tipoPago
            proceso.usuario = session.usuario
            /*TODO preguntar que es esto?*/
            //proceso.tipoComprobanteId = TipoComprobanteId.get(3)
            proceso.valor = pago.monto
            proceso.impuesto = 0
            proceso.documento = pago.referencia
            proceso.pagoAux = pago
            proceso.tipoProceso = "P"
            proceso.empresa = session.empresa
            println "valor " + proceso.valor + " inp " + proceso.impuesto
            if (proceso.save(flush: true)) {
                procesoService.registrar_old(proceso, session.perfil, session.usuario, session.contabilidad)
                render "ok"
            } else {
                println "error en el proceso " + proceso.errors
                render "error"
            }

        } else {
            render "error"
            println "error save pago " + pago.errors
        }
    }

    def prueba() {
        render "prueba"
    }


    def borrarProceso() {
        println("LOG: borrar proceso " + params)
        def proceso = Proceso.get(params.id)
        def comprobante = Comprobante.findByProceso(proceso)
        def asiento
        if (comprobante) {
            asiento = Asiento.findAllByComprobante(comprobante)
        }
        if (comprobante) {
            if (comprobante.registrado == 'N') {
                def msn = kerberosoldService.ejecutarProcedure("mayorizar", [comprobante.id, -1])
                println "LOG: desmayorizando  comprobante borrar proceso ${comprobante.id} " + msn["mayorizar"]
                try {
                    def log = new LogMayorizacion()
                    log.usuario = cratos.seguridad.Persona.get(session.usuario.id)
                    log.comprobante = comprobante
                    log.tipo = "B"
                    log.resultado = msn["mayorizar"].toString()
                    log.save(flush: true)
                } catch (e) {
                    println "LOG: error del login de mayorizar " + msn["mayorizar"].toString()
                }
                proceso.estado = "B"
                proceso.save(flush: true)
                comprobante.registrado = "B"
                comprobante.save(flush: true)
                flash.message = "Proceso Borrado!"
                redirect(action: 'lsta')
            } else {
                flash.message = "No se puede borrar el proceso!!"
                redirect(action: 'lsta')
            }

        } else {
            proceso.estado = "B"
            proceso.save(flush: true)
            flash.message = "Proceso Borrado!"
            redirect(action: 'lsta')
        }
    }


    def procesosAnulados() {
//        println "proc anulados "+params
        def contabilidad
        if (!params.contabilidad) {
            contabilidad = session.contabilidad
        } else {
            contabilidad = Contabilidad.get(params.contabilidad)
        }
//        println "contabilidad "+contabilidad
        def procesos = Proceso.findAllByEstadoAndContabilidad("B", contabilidad, [sort: "fecha"])
        [procesos: procesos, contabilidad: contabilidad]
    }

    def verComprobante() {
        def comp = Comprobante.get(params.id)
        def asientos = Asiento.findAllByComprobante(comp)
        [asientos: asientos, comp: comp]
    }


    def detalleSri() {

        def empresa = Empresa.get(session.empresa.id)
        def proceso = Proceso.get(params.id)
        def retencion = Retencion.findByProceso(proceso)
        def libreta = DocumentoEmpresa.findAllByEmpresaAndFechaInicioLessThanEqualsAndFechaFinGreaterThanEqualsAndTipo(empresa, new Date(), new Date(),'R')

        def baseImponible = (proceso?.baseImponibleIva ?: 0) + (proceso?.baseImponibleIva0 ?: 0) + (proceso?.baseImponibleNoIva ?: 0)

        return [proceso: proceso, libreta: libreta, retencion: retencion, base: baseImponible]





        //antiguo

//        def proceso = Proceso.get(params.id)
//
//        def retencion = Retencion.findByProceso(proceso)
//
//
//        def detalleRetencion = []
//        if (retencion) {
//            detalleRetencion = DetalleRetencion.findAllByRetencion(retencion)
//        } else {
//            detalleRetencion = []
//        }
//
//        def hoy = new Date()
//        def anioFin = hoy.format("yyyy").toInteger()
//        def anios = []
//        3.times {
//            def p = getPeriodosByAnio(anioFin - it)
//            if (p.size() > 0) {
//                anios.add(anioFin - it)
//            }
//        }
//        def per = Periodo.withCriteria {
//            ge("fechaInicio", new Date().parse("dd-MM-yyyy", "01-01-" + hoy.format("yyyy")))
//            le("fechaFin", utilitarioService.getLastDayOfMonth(hoy))
//            order("fechaInicio", "asc")
//        }
//        def periodos = []
//        per.each { p ->
//            def key = p.fechaInicio.format("MM")
//            def val = fechaConFormato(p.fechaInicio, "MMMM yyyy").toUpperCase()
//            def m = [:]
//            m.id = key
//            m.val = val
//            periodos.add(m)
//        }
//
//
//        def sustento = SustentoTributario.list()
//
//        def porIce = Impuesto.findAllBySri('ICE')
//        def porBns = Impuesto.findAllBySri('BNS')
//        def porSrv = Impuesto.findAllBySri('SRV')
//
//        def comprobante = Comprobante.findByProceso(proceso)
//        def asiento = Asiento.findAllByComprobante(comprobante)
//
////        println("-->>>" + asiento)
//
//
//        return [proceso: proceso, periodos: periodos, sustento: sustento, porIce: porIce, porBns: porBns, porSrv: porSrv, anios: anios, detalleRetencion: detalleRetencion, retencion: retencion]
    }

    def getPeriodosByAnio(anio) {
        def per = Periodo.withCriteria {
            ge("fechaInicio", new Date().parse("dd-MM-yyyy", "01-01-" + anio))
            le("fechaFin", new Date().parse("dd-MM-yyyy", "31-12-" + anio))
            order("fechaInicio", "asc")
        }
        def periodos = []
        per.each { p ->
            def key = p.fechaInicio.format("MM")
            def val = fechaConFormato(p.fechaInicio, "MMMM yyyy").toUpperCase()
            def m = [:]
            m.id = key
            m.val = val
            periodos.add(m)
        }
        return periodos
    }


    def getPeriodos() {
//        println "getPeriodos " + params
        def periodos = getPeriodosByAnio(params.anio)
        render g.select(name: "mes", from: periodos, optionKey: "id", optionValue: "val")
    }


    def getPorcentajes() {

        def concepto = ConceptoRetencionImpuestoRenta.get(params.id)
        render g.textField(name: "porcentajeIR", value: concepto?.porcentaje, style: "width: 50px")

    }

    private String fechaConFormato(fecha, formato) {
        def meses = ["", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"]
        def meses2 = ["", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"]
        def strFecha
        switch (formato) {
            case "MMM-yy":
                strFecha = meses[fecha.format("MM").toInteger()] + "-" + fecha.format("yy")
                break;
            case "MMMM-yy":
                strFecha = meses2[fecha.format("MM").toInteger()] + "-" + fecha.format("yy")
                break;
            case "MMMM yyyy":
                strFecha = meses2[fecha.format("MM").toInteger()] + " " + fecha.format("yyyy")
                break;
        }
        return strFecha
    }

    def guardarSri() {
//        println("guardarSri:" + params)
        params.lang="en"
        def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
        def localeResolver = request.getAttribute(key)
        localeResolver.setLocale(request, response, new Locale("en"))
        def fecha = params.remove("fechaEmision")
        def proceso = Proceso.get(params.id)
        def retencion = Retencion.findByProceso(proceso)
        def concepto = ConceptoRetencionImpuestoRenta.get(params.concepto)

        if (retencion.save(flush: true)) {
            retencion.numeroEstablecimiento = params.numeroEstablecimiento
            retencion.numeroPuntoEmision = params.numeroEmision
            retencion.numeroAutorizacionComprobante = params.numeroAutorizacion
            retencion.tipoPago = params.pago
            retencion.numeroSecuencial = params.numeroSecuencial
            retencion.creditoTributario = params.credito
            if (params.pago == '02') {
                retencion.normaLegal = params.normaLegal
                retencion.convenio = params.convenio
                retencion.pais = Pais.get(params.pais)
            } else {
                retencion.normaLegal = ''
                retencion.convenio = ''
            }
            if (fecha) {
                retencion.fechaEmision = new Date().parse("dd-MM-yyyy", fecha)
            }
            //detalle
            def detalle = DetalleRetencion.findAllByRetencion(retencion)
            detalle.each { det ->
                if (det.cuenta.impuesto.sri == 'RNT') {
//                   println("entro RNT!")
                    det.porcentaje = params.porcentaje
                    det.conceptoRetencionImpuestoRenta = params.concepto
                    det.base = params.base
                    det.total = params.valorRetenido
                }
                if (det.cuenta.impuesto.sri == 'ICE') {
//                   println("entro ICE!")
                    det.porcentaje = params.icePorcentaje.toDouble()
                    det.base = params.iceBase.toDouble()
                    det.total = params.valorRetenidoIce.toDouble()
                }
                if (det.cuenta.impuesto.sri == 'BNS') {
//                   println("entro BNS!")
                    det.porcentaje = params.bienesPorcentaje.toDouble()
                    det.base = params.bienesBase.toDouble()
                    det.total = params.valorRetenidoBienes.toDouble()
                }
                if (det.cuenta.impuesto.sri == 'SRV') {
//                   println("entro SRV!")
                    det.porcentaje = params.serviciosPorcentaje.toDouble()
                    det.base = params.serviciosBase.toDouble()
                    det.total = params.valorRetenidoServicios.toDouble()
                } else {
//                   println("NO entro!")
                }
            }
            render "ok"
//            println("ok")

        } else {
//            println("error al grabar la retencion" + retencion.errors)
            render "Error al grabar!"
        }
    }

    def pagos_ajax () {
        def proceso = Proceso.get(params.proceso)
        def formasPago = ProcesoFormaDePago.findAllByProceso(proceso).tipoPago
        def listaId = TipoPago.list().id - formasPago.id
        def listaFormasPago = TipoPago.findAllByIdInList(listaId)
        return [proceso: proceso, lista: listaFormasPago]
    }

    def tablaPagos_ajax () {
        def proceso = Proceso.get(params.proceso)
        def formasPago = ProcesoFormaDePago.findAllByProceso(proceso)
        return [formas: formasPago, proceso: proceso]
    }

    def borrarFormaPago_ajax () {
        def formaPago = ProcesoFormaDePago.get(params.id)

        try{
            formaPago.delete(flush: true)
            render "ok"
        }catch (e){
            render "no"
            println("error borrar forma pago " + formaPago.errors)
        }
    }

    def listaFormas_ajax () {
        def proceso = Proceso.get(params.proceso)
        def formasPago = ProcesoFormaDePago.findAllByProceso(proceso).tipoPago
        def listaId = TipoPago.list().id - formasPago.id
        def listaFormasPago = TipoPago.findAllByIdInList(listaId)
        return [proceso: proceso, lista: listaFormasPago]
    }

    def agregarFormaPago_ajax () {
//        println("params " + params)
        def proceso = Proceso.get(params.proceso)
        def tipoPago = TipoPago.get(params.forma)
        def formaPago = new ProcesoFormaDePago()
        formaPago.proceso = proceso
        formaPago.tipoPago = tipoPago

        if(!formaPago.save(flush: true)){
            render "no"
        }else{
            render "ok"
        }
    }

    def cargarPagoMain_ajax () {
        def proceso = Proceso.get(params.proceso)
        def formasPago = ProcesoFormaDePago.findAllByProceso(proceso).tipoPago
        return [formasPago: formasPago]
    }

    def comprobante_ajax () {
        def proceso = Proceso.get(params.proceso)
        def comprobantes = Comprobante.findAllByProceso(proceso).sort{it.tipo.descripcion}
        return [comprobantes: comprobantes, proceso: proceso]
    }

    def asientos_ajax () {
        def proceso = Proceso.get(params.proceso)
        def comprobante = Comprobante.get(params.comprobante)
        def asientos = Asiento.findAllByComprobante(comprobante).sort{it.numero}
        def auxiliares = Auxiliar.findAllByAsientoInList(asientos)
        return [asientos: asientos, comprobante: comprobante, proceso: proceso, auxiliares: auxiliares]
    }

    def formAsiento_ajax () {
//        println("params asiento " + params)
        def comprobante = Comprobante.get(params.comprobante)
        if(params.asiento){
            def asiento = Asiento.get(params.asiento)
            return [asiento: asiento]
        }else{
            return [comprobante: comprobante]
        }
    }


    def buscarCuenta_ajax () {
        def empresa = Empresa.get(params.empresa)
        return [empresa: empresa]
    }

    def tablaBuscarCuenta_ajax () {
//        println("params " + params)
        def empresa = Empresa.get(params.empresa)
        def res

        if(params.nombre == "" && params.codigo == ""){
            res = Cuenta.findAllByEmpresa(empresa).sort{it.numero}
        }else{
            res = Cuenta.withCriteria {
                eq("empresa", empresa)

                and{
                    ilike("descripcion", '%' + params.nombre + '%')
                    ilike("numero", '%' + params.codigo + '%')
                }
                order ("numero","asc")
            }
        }

        return [cuentas: res]
    }

    def guardarAsiento_ajax () {
//        println("params guardar " + params)
        def asiento
        def cuenta = Cuenta.get(params.cuenta)
        def proceso = Proceso.get(params.proceso)
        def comprobante = Comprobante.get(params.comprobante)
        def asientos = Asiento.findAllByComprobante(comprobante).sort{it.numero}
        def siguiente = 0
        if(asientos){
            siguiente = asientos.numero.last() + 1
        }
//        println("asientos " + asientos.numero)

        if(params.asiento){
            asiento = Asiento.get(params.asiento)
            asiento.cuenta = cuenta
            asiento.debe = params.debe.toDouble()
            asiento.haber = params.haber.toDouble()
        }else{
            asiento = new Asiento()
            asiento.cuenta = cuenta
            asiento.debe = params.debe.toDouble()
            asiento.haber = params.haber.toDouble()
            asiento.comprobante = comprobante
            asiento.numero = siguiente
        }

        if(!asiento.save(flush: true)){
            render "no"
            println("error " + asiento.errors)
        }else{
            render "ok"
        }
    }

    def borrarAsiento_ajax () {
//        println("borrar asiento params " + params)
        def comprobante = Comprobante.get(params.comprobante)
        def asiento = Asiento.get(params.asiento)
        def auxiliar = Auxiliar.findByAsiento(asiento)

        if(comprobante.registrado == 'N'){
            if(!auxiliar){
                try{
                    asiento.delete(flush: true)
                    render "ok_Asiento borrado correctamente"
                }catch (e){
                    render "no_Error al borrar el asiento"
                }
            } else{
                render "no_No se puede borrar el asiento, debido a que posee un auxiliar"
            }
        }else{
            render "no_No se puede borrar el asiento, el comprobante ya se encuentra registrado"
        }
    }

    def borrarAuxiliar_ajax () {
        def comprobante = Comprobante.get(params.comprobante)
        def auxiliar = Auxiliar.get(params.auxiliar)

        if(comprobante.registrado == 'N'){
            try{
                auxiliar.delete(flush: true)
                render "ok_Auxiliar borrado correctamente"
            }catch (e){
                render "no_Error al borrar el auxiliar"
            }
        }else{
            render "no_No se puede borrar el auxiliar, el comprobante ya se encuentra registrado"
        }
    }

    def formAuxiliar_ajax() {
        def comprobante = Comprobante.get(params.comprobante)
        def asiento
        def auxiliar
        if(params.auxiliar){
            auxiliar = Auxiliar.get(params.auxiliar)
            asiento = auxiliar.asiento
            return [asiento: asiento, auxiliar: auxiliar, comprobante: comprobante]
        }else{
            asiento = Asiento.get(params.asiento)
            return [asiento: asiento, comprobante: comprobante]
        }
    }

    def guardarAuxiliar_ajax () {
//        println("params " + params)
        def asiento
        def comprobante = Comprobante.get(params.comprobante)
        def tipoPago = TipoPago.get(params.tipoPago)
        def proveedor = Proveedor.get(params.proveedor)
        def fechaPago =  new Date().parse("dd-MM-yyyy", params.fechaPago)
        def auxiliar

        if(params.auxiliar){
            auxiliar = Auxiliar.get(params.auxiliar)
        }else{
            asiento = Asiento.get(params.asiento)
            auxiliar = new Auxiliar()
            auxiliar.asiento = asiento
        }

        auxiliar.descripcion = params.descripcion
        auxiliar.fechaPago = fechaPago
        auxiliar.proveedor = proveedor
        auxiliar.tipoPago = tipoPago
        auxiliar.debe = params.debe.toDouble()
        auxiliar.haber = params.haber.toDouble()

        try{
            auxiliar.save(flush: true)
            render "ok"
        }catch (e){
            render "no"
        }
    }

    def tablaBuscarComprobante_ajax () {
        println "tablaBuscarComprobante_ajax: $params"
        def cn = dbConnectionService.getConnection()
        def proveedor = Proveedor.get(params.proveedor)
        def sql
        if(params.tipo == 'P') {
            sql = "select * from porpagar(${proveedor?.id}) where sldo <> 0;"
        } else if(params.tipo == 'N') {
            sql = "select cmpr__id, clntnmbr prvenmbr, dscr, debe hber, ntcr pgdo, sldo from ventas(${proveedor?.id}) " +
                    "where sldo <> 0"
        }

        def res = cn.rows(sql.toString())
        return [res: res]
    }

    def filaComprobante_ajax () {
        println "filaComprobante_ajax: $params"
        def proceso = Proceso.get(params.proceso)
//        def comprobante = Comprobante.get(params.comprobante)
//        def proveedor =
        def res
        if(params.proceso){
            def cn = dbConnectionService.getConnection()
            def sql
            sql = "select sldo from porpagar(${proceso?.proveedor?.id}) where cmpr__id = ${proceso?.comprobante?.id} ;"
            res = cn.firstRow(sql.toString())
        }else{
//            def cn = dbConnectionService.getConnection()
//            def sql
//            sql = "select sldo from porpagar(${proceso?.proveedor?.id}) where cmpr__id = ${comprobante?.id} ;"
//            res = cn.firstRow(sql.toString())
        }
        return[proceso: proceso, saldo: res?.sldo]
    }

    def valores_ajax () {
        def proceso = Proceso.get(params.proceso)
        return[proceso: proceso, tipo: params.tipo]
    }

    def proveedor_ajax () {
        def proceso = Proceso.get(params.proceso)
        def proveedores = Proveedor.list()
        def tr
        def prve
        if(params.id) {
            prve = Proveedor.get(params.id.toInteger())
        }

        switch (params.tipo) {
            case "P":
                tr = TipoRelacion.findAllByCodigoInList(['C','P'])
                proveedores = Proveedor.findAllByTipoRelacionInList(tr)
                break
            case "NC" :
                tr = TipoRelacion.findAllByCodigoInList(['C','E'])
                proveedores = Proveedor.findAllByTipoRelacionInList(tr)
                break
            case "V"  :
                tr = TipoRelacion.findAllByCodigoInList(['C','E'])
                proveedores = Proveedor.findAllByTipoRelacionInList(tr)
                break
        }

//        println("proveedores " + proveedores)
        return [proveedores : proveedores, proceso: proceso, tipo: params.tipo, proveedor: prve]
    }

    def cambiarContabilidad_ajax () {
        def usuario = Persona.get(session.usuario.id)
        def contabilidad = Contabilidad.get(session.contabilidad.id)
        def empresa = Empresa.get(session.empresa.id)
        def contabilidades = Contabilidad.findAllByInstitucion(empresa, [sort: "fechaInicio"])
        contabilidades.remove(contabilidad)
        return [usuario: usuario, contabilidad: contabilidad, contabilidades: contabilidades]
    }

    def botonesMayo_ajax () {
        def comprobante = Comprobante.get(params.comprobante).refresh()
        def auxiliares = Auxiliar.findAllByComprobante(comprobante)
        return[comprobante: comprobante, auxiliares: auxiliares]
    }
    def mayorizar_ajax () {
//        println("params " + params)
        def comprobante = Comprobante.get(params.id)
        def res = procesoService.mayorizar(comprobante)
//        println("res " + res)
        render res
    }

    def desmayorizar_ajax () {
//        println("params " + params)
        def comprobante = Comprobante.get(params.id)
        def res = procesoService.desmayorizar(comprobante)
//        println("res " + res)
        render res
    }

    def numeracion_ajax () {
        def documentoEmpresa = DocumentoEmpresa.get(params.libretin)
        return [libreta : documentoEmpresa]
    }

    def buscarPrcs() {
//        println "busqueda "
    }

    def tablaBuscarPrcs() {
        println "buscar .... $params"
        def data = []
        def cn = dbConnectionService.getConnection()
        def wh = ""
        def buscar = params.buscar.trim()
        if(params.buscar) {
            session.buscar = buscar
            wh = " and prcsdscr ilike '%${buscar}%' "
        }
/*
        def sql = "select prcs__id id, prcsfcha, prcsdscr, prcsetdo, cmprnmro, tptrdscr, prvenmbr " +
                "from prcs, cmpr, prve, tptr where prcs.empr__id = ${session.empresa.id} and " +
                "cmpr.prcs__id = prcs.prcs__id and prve.preve__id = prcs.prve__id and " +
                "tptr.tptr__id = prcs.tptr__id" +
                "order by prcsfcha"
*/
        def sql = "select prcs.prcs__id id, prcsfcha, prcsdscr, prcsetdo, cmprprfj||cmprnmro cmprnmro, " +
                "prcstpps, prvenmbr, prcsetdo " +
                "from prcs left join cmpr on cmpr.prcs__id = prcs.prcs__id, prve where prcs.empr__id = ${session.empresa.id} and " +
                "prve.prve__id = prcs.prve__id ${wh}" +
                "order by prcsfcha limit 21"

//        println "buscar .. ${sql}"

        data = cn.rows(sql.toString())

        def tpps = ["P": "Pago", "C": "Compra", "V": "Venta", "A": "Ajuste", "O": "Otro"]

        def msg = ""
        if(data?.size() > 20){
            data.pop()   //descarta el último puesto que son 21
            msg = "<div class='alert-danger' style='margin-top:-20px; diplay:block; height:25px;margin-bottom: 20px;'>" +
                    " <i class='fa fa-warning fa-2x pull-left'></i> Su búsqueda ha generado más de 20 resultados. " +
                    "Use más letras para especificar mejor la búsqueda.</div>"
        }
        cn.close()

        return [data: data, msg: msg, tpps: tpps]
    }

    def validarSerie_ajax () {
        def retencion
        def todas

        if(params.retencion){
            retencion = Retencion.get(params.retencion)
            todas = Retencion.findAllByEmpresa(retencion.empresa) - retencion
            if(todas.numero.contains(params.serie)){
                render 'no'
            }else{
                render 'ok'
            }
        }else{
            render 'ok'
        }
    }

    def concepto_ajax () {
        def concepto = ConceptoRetencionImpuestoRenta.get(params.idConcepto)
        def valorRetenido = (params.base ? params.base.toDouble() : 0) * (concepto.porcentaje.toDouble() / 100)
        return [concepto: concepto, retenido: valorRetenido]
    }

    def conceptoSV_ajax () {
        def concepto = ConceptoRetencionImpuestoRenta.get(params.idConcepto)
        def valorRetenido = (params.base ? params.base.toDouble() : 0) * (concepto.porcentaje.toDouble() / 100)
        return [concepto: concepto, retenido: valorRetenido]
    }

    def validarBase_ajax () {
        println("params " + params)
        def sumatoria = params.baseBienes.toDouble() + params.baseServicios.toDouble()
        if(params.baseBienes.toDouble() > params.baseImponible.toDouble() || params.baseBienes.toDouble() > sumatoria ){
            render false
        }else{
            render true
        }
    }

    def porcentaje_ajax () {
        def porcentaje = PorcentajeIva.get(params.porcentaje)
        return [porcentaje: porcentaje.valor]
    }

    def calcularValorICE_ajax () {
//        def porcentaje = PorcentajeIva.get(params.porcentaje)
//        def valor = params.base.toDouble() * (porcentaje.valor / 100)
        def valor = (params.base ? params.base.toDouble() : 0) * (params.porcentaje ? (params.porcentaje.toDouble()  / 100) : 0)
        return [valor: valor]
    }

    def calcularValorBI_ajax () {
//       def porcentaje = PorcentajeIva.get(params.porcentaje)
//       def valor = params.base.toDouble() * (porcentaje.valor / 100)
        def valor = (params.base ? params.base.toDouble() : 0) * (params.porcentaje ? (params.porcentaje.toDouble()  / 100) : 0)
        return [valor: valor]
    }

    def calcularValorSV_ajax () {
//       def porcentaje = PorcentajeIva.get(params.porcentaje)
//       def valor = params.base.toDouble() * (porcentaje.valor / 100)
        def valor = (params.base ? params.base.toDouble() : 0) * (params.porcentaje ? (params.porcentaje.toDouble() / 100) : 0)
        return [valor: valor]
    }

    def totalBase_ajax () {
        def total = (params.ice ? params.ice.toDouble() : 0) + (params.bienes ? params.bienes.toDouble() : 0) + (params.servicios ? params.servicios.toDouble() : 0)
        def base = params.base.toDouble()
        return [total: total.toDouble(), base: base]
    }

    def totalesRenta_ajax () {
        def total = (params.bienes ? params.bienes.toDouble() : 0) + (params.servicios ? params.servicios.toDouble() : 0)
        def base = params.base.toDouble()
        return [total: total.toDouble(), base: base]
    }

    def comprobarSerie_ajax () {
        def documentoEmpresa = DocumentoEmpresa.get(params.libretin)
        def desde = documentoEmpresa.numeroDesde
        def hasta = documentoEmpresa.numeroHasta

        if((params.serie.toInteger() >= desde.toInteger()) && (params.serie.toInteger() <= hasta.toInteger())){
            render 'ok'
        }else{
            render 'no'
        }
    }

    def saveRetencion_ajax () {
//        println("params save " + params)
        def proceso = Proceso.get(params.proceso)
        def retencion
        def conceptoBienes
        def conceptoServicios
        def proveedor = Proveedor.get(proceso.proveedor.id)
        def libretin
        def porcentajeIva
        if(params.retencion){
            retencion = Retencion.get(params.retencion)
        }else{
            retencion = new Retencion()
            retencion.proceso = proceso
            retencion.proveedor = proveedor
        }
        retencion.direccion = params.direccion
        retencion.telefono = params.telefono
        retencion.fecha = new Date().parse("dd-MM-yyyy",params.fecha)
        retencion.fechaEmision = new Date().parse("dd-MM-yyyy",params.fechaEmision)
        conceptoBienes = ConceptoRetencionImpuestoRenta.get(params.conceptoRIRBienes)
        retencion.proveedor
        retencion.conceptoRIRBienes = conceptoBienes
        retencion.baseRenta = (params.baseRenta ? params.baseRenta.toDouble() : 0)
        retencion.renta = params.renta.toDouble()
        retencion.empresa = proceso.empresa
        if(params.conceptoRIRBienes != '23'){
            conceptoServicios = ConceptoRetencionImpuestoRenta.get(params.conceptoRIRServicios)
            retencion.conceptoRIRServicios = conceptoServicios
            retencion.baseRentaServicios = (params.baseRentaServicios ? params.baseRentaServicios.toDouble() : 0)
            retencion.rentaServicios = params.servicios.toDouble()
            libretin = DocumentoEmpresa.get(params.documentoEmpresa)
            retencion.documentoEmpresa = libretin
            retencion.numero = params.numeroComprobante.toInteger()
            retencion.numeroComprobante = (libretin.numeroEstablecimiento + "-" + libretin.numeroEmision + "-" + params.numeroComprobante)
            porcentajeIva = PorcentajeIva.get(params.porcentajeIva)
            retencion.porcentajeIva = porcentajeIva
                if(porcentajeIva.codigo.toInteger() != 0){
                    retencion.baseIva = params.iva.toDouble()
                    retencion.creditoTributario = params.creditoTributario
                    retencion.baseIce = (params.baseIce ? params.baseIce.toDouble() : 0)
                    retencion.porcentajeIce = (params.porcentajeIce ? params.porcentajeIce.toDouble() : 0)
                    retencion.ice = params.ice.toDouble()
                    retencion.baseBienes = (params.baseBienes ? params.baseBienes.toDouble() : 0)
                    retencion.porcentajeBienes = (params.porcentajeBienes ? params.porcentajeBienes.toDouble(): 0)
                    retencion.bienes = params.bienes.toDouble()
                    retencion.servicios = params.servicios.toDouble()
                    retencion.porcentajeServicios = (params.baseServicios ? params.porcentajeServicios.toDouble() : 0)
                    retencion.baseServicios = (params.baseServicios ? params.baseServicios.toDouble() : 0 )
                    retencion.pago = params.pago
                        if(params.pago == '02'){
                            retencion.pais = Pais.get(params.pais)
                            retencion.convenio = params.convenio
                            retencion.normaLegal = params.normaLegal
                        }
                }else{
                    retencion.baseIce = 0
                    retencion.porcentajeIce = 0
                    retencion.ice = 0
                    retencion.baseBienes = 0
                    retencion.porcentajeBienes = 0
                    retencion.bienes = 0
                    retencion.servicios = 0
                    retencion.porcentajeServicios = 0
                    retencion.baseServicios = 0
                }
        }else{
            retencion.baseRentaServicios = 0
            retencion.rentaServicios = 0
        }

        try {
            retencion.save(flush: true)
            render "ok"
        }catch (e){
            println("errores " + e)
            render "no"
        }

    }

    def numeracionFactura_ajax () {
        def documentoEmpresa = DocumentoEmpresa.get(params.libretin)
        return [libreta : documentoEmpresa]
    }

    def comprobarSerieFactura_ajax () {
        def documentoEmpresa = DocumentoEmpresa.get(params.libretin)
        def desde = documentoEmpresa.numeroDesde
        def hasta = documentoEmpresa.numeroHasta

        if((params.serie.toInteger() >= desde.toInteger()) && (params.serie.toInteger() <= hasta.toInteger())){
            render 'ok'
        }else{
            render 'no'
        }
    }

    def validarSerieFactura_ajax () {
        def proceso
        def todas
        def empresaF = Empresa.get(session.empresa.id)

        if(params.proceso){
            proceso = Proceso.get(params.retencion)
            todas = Proceso.findAllByEmpresa(proceso.empresa) - proceso
            if(todas.facturaSecuencial.contains(params.serie.toInteger())){
                render 'no'
            }else{
                render 'ok'
            }
        }else{
            todas = Proceso.findAllByEmpresa(empresaF)
            if(todas.facturaSecuencial.contains(params.serie.toInteger())){
                render 'no'
            }else{
                render 'ok'
            }
        }
    }
}

