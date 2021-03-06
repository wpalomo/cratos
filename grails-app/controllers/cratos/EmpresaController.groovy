package cratos

import cratos.seguridad.Persona


class EmpresaController extends cratos.seguridad.Shield {

    static allowedMethods = [save: "POST", delete: "POST", save_ajax: "POST", delete_ajax: "POST"]

    def index() {
        redirect(action: "list", params: params)
    } //index

    def list() {
        def usro = Persona.get(session?.usuario?.id)
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        def empresaInstanceList = Empresa.list(params)
        def empresaInstanceCount = Empresa.count()
        if (empresaInstanceList.size() == 0 && params.offset && params.max) {
            params.offset = params.offset - params.max
        }
        empresaInstanceList = Empresa.list(params)


//        def miEmpresa = session.empresa

        def empresa
//        println "usuario: ${session}, usro: ${usro.login}"

        if(usro.login == 'admin') {
            empresa = Empresa.list([sort: 'nombre'])
        } else {
            empresa = Empresa.findAllById(session.empresa.id)
        }

        println("empresa:" + empresa)


        def empresaLogin = Empresa.get(session.empresa.id)

        return [empresaInstanceList: empresaInstanceList, empresaInstanceCount: empresaInstanceCount, empresa: empresa, empresaLogin: empresaLogin]
    } //list

    def show_ajax() {
        if (params.id) {
            def empresaInstance = Empresa.get(params.id)
            if (!empresaInstance) {
                notFound_ajax()
                return
            }
            return [empresaInstance: empresaInstance]
        } else {
            notFound_ajax()
        }
    } //show para cargar con ajax en un dialog

    def form_ajax() {
        def empresaInstance = new Empresa(params)
        def emision = ['F': 'Física', 'E':'Electrónica']
        if (params.id) {
            empresaInstance = Empresa.get(params.id)
            if (!empresaInstance) {
                notFound_ajax()
                return
            }
        }
        return [empresaInstance: empresaInstance, emision: emision]
    } //form para cargar con ajax en un dialog

    def listAdmin() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        def empresaInstanceList = Empresa.list(params)
        def empresaInstanceCount = Empresa.count()
        if (empresaInstanceList.size() == 0 && params.offset && params.max) {
            params.offset = params.offset - params.max
        }
        empresaInstanceList = Empresa.list(params)
        return [empresaInstanceList: empresaInstanceList, empresaInstanceCount: empresaInstanceCount]
    } //list

    def showAdmin_ajax() {
        if (params.id) {
            def empresaInstance = Empresa.get(params.id)
            if (!empresaInstance) {
                notFound_ajax()
                return
            }
            return [empresaInstance: empresaInstance]
        } else {
            notFound_ajax()
        }
    } //show para cargar con ajax en un dialog

    def formAdmin_ajax() {
        def empresaInstance = new Empresa(params)
        if (params.id) {
            empresaInstance = Empresa.get(params.id)
            if (!empresaInstance) {
                notFound_ajax()
                return
            }
        }
        return [empresaInstance: empresaInstance]
    } //form para cargar con ajax en un dialog


    def validarRuc_ajax() {
        params.ruc = params.ruc.toString().trim()
        if (params.id) {
            def empr = Empresa.get(params.id)
            if (empr.ruc == params.ruc) {
                render true
                return
            } else {
                render Empresa.countByRuc(params.ruc) == 0
                return
            }
        } else {
            render Empresa.countByRuc(params.ruc) == 0
            return
        }
    }

    def save_ajax() {
//        println("params " + params)
        def empresaInstance = new Empresa()
        if (params.id) {
            empresaInstance = Empresa.get(params.id)
            if (!empresaInstance) {
                notFound_ajax()
                return
            }
        } //update
        params.numeroComprobanteDiario = params.numeroComprobanteDiario.toInteger()
        params.numeroComprobanteEgreso = params.numeroComprobanteEgreso.toInteger()
        params.numeroComprobanteIngreso = params.numeroComprobanteIngreso.toInteger()
        empresaInstance.properties = params

        if(params.obligadaContabilidad_name){
            empresaInstance.obligadaContabilidad = '1'
        }else{
            empresaInstance.obligadaContabilidad = '0'
        }

        if(params.ambiente_name){
            empresaInstance.ambiente = '2'
        }else{
            empresaInstance.ambiente = '1'
        }

        if (!empresaInstance.save(flush: true)) {
            def msg = "NO_No se pudo ${params.id ? 'actualizar' : 'crear'} Empresa."
            msg += renderErrors(bean: empresaInstance)
            render msg
            return
        }
        render "OK_${params.id ? 'Actualización' : 'Creación'} de Empresa exitosa."
    } //save para grabar desde ajax

    def delete_ajax() {
        if (params.id) {
            def empresaInstance = Empresa.get(params.id)
            if (empresaInstance) {
                try {
                    empresaInstance.delete(flush: true)
                    render "OK_Eliminación de Empresa exitosa."
                } catch (e) {
                    render "NO_No se pudo eliminar Empresa."
                }
            } else {
                notFound_ajax()
            }
        } else {
            notFound_ajax()
        }
    } //delete para eliminar via ajax

    protected void notFound_ajax() {
        render "NO_No se encontró Empresa."
    } //notFound para ajax

    def firma_ajax() {
        def empresa = Empresa.get(params.id)
        return [empresaInstance: empresa]
    }

    def guardarFirma_ajax () {
//        println("params " + params)

        def empresa = Empresa.get(params.id)
        empresa.clave = params.clave

        try{
            empresa.save(flush: true)
            render "ok"
        }catch (e){
            println("error al guardar la clave de la firma " + e)
            render "no"
        }
    }

    def sucursales_ajax () {
        def empresa = Empresa.get(params.id)
        return [empresa: empresa]
    }

    def tablaSucursales_ajax () {
        def empresa = Empresa.get(params.id)
        def sucursales = Establecimiento.findAllByEmpresa(empresa).sort{it.numero}
        return[sucursales: sucursales]
    }

    def guardarSucursal_ajax () {
//        println("params suc " + params)

        def empresa = Empresa.get(params.id)
        def sucursal

        if(params.sucursal != ''){
            sucursal = Establecimiento.get(params.sucursal)
        }else{
            sucursal = new Establecimiento()
            sucursal.empresa = empresa
        }

        sucursal.direccion = params.direccion
        sucursal.numero = params.numero
        sucursal.nombre = params.nombre

        try{
            sucursal.save(flush: true)
            render "ok"
        }catch (e){
            println("error al guardar la sucursal " + e)
            render "no"
        }
    }

    def borrarSucursal_ajax () {
        def establecimiento = Establecimiento.get(params.id)

        try{
            establecimiento.delete(flush: true)
            render "ok"
        }catch (e){
            println("error al borrar la sucursal")
            render "no"
        }
    }

}
