package cratos

import cratos.seguridad.Persona
import org.springframework.dao.DataIntegrityViolationException

class ContabilidadController extends cratos.seguridad.Shield {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", delete: "GET"]

    def utilitarioService

    def cambiar() {
        def yo = Persona.get(session.usuario.id)
        def cont = Contabilidad.get(session.contabilidad.id)
        def empresa = Empresa.get(session.empresa.id)

        def contabilidades = Contabilidad.findAllByInstitucion(empresa, [sort: "fechaInicio"])
        contabilidades.remove(cont)

        return [yo: yo, cont: cont, contabilidades: contabilidades]
    }

    def cambiarContabilidad() {
        def contabilidad = Contabilidad.get(params.contabilidad)
        session.contabilidad = contabilidad
        redirect controller: 'proceso', action: 'buscarPrcs'
    }

    /* ************************ COPIAR DESDE AQUI ****************************/

    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        params.sort = 'fechaInicio'

        def contabilidadInstanceList = Contabilidad.findAllByInstitucion(session.empresa, params)
        def contabilidadInstanceCount = Contabilidad.count()
        if (contabilidadInstanceList.size() == 0 && params.offset && params.max) {
            params.offset = params.offset - params.max
        }
        contabilidadInstanceList = Contabilidad.findAllByInstitucion(session.empresa, params).sort{it.descripcion}
//        contabilidadInstanceList = contabilidadInstanceList.sort{it.fechaInicio}

        return [contabilidadInstanceList: contabilidadInstanceList, contabilidadInstanceCount: contabilidadInstanceCount]
    } //list

    def show_ajax() {
        if (params.id) {
            def contabilidadInstance = Contabilidad.get(params.id)
            if (!contabilidadInstance) {
                notFound_ajax()
                return
            }
            return [contabilidadInstance: contabilidadInstance]
        } else {
            notFound_ajax()
        }
    } //show para cargar con ajax en un dialog

    def form_ajax() {
        def contabilidadInstance = new Contabilidad(params)
        def empresa = Empresa.get(session.empresa.id)
        if (params.id) {
            contabilidadInstance = Contabilidad.get(params.id)
            if (!contabilidadInstance) {
                notFound_ajax()
                return
            }
        }

        def cuentas = Cuenta.withCriteria {
            ilike("numero", '3%')
            eq("empresa", empresa)
            order("descripcion","asc")
        }
        return [contabilidadInstance: contabilidadInstance, cuentas: cuentas]
    } //form para cargar con ajax en un dialog

    def save_ajax() {
//        println("params save " + params)

        def errores = ''
        def contabilidadInstance

        if(params.id){
            contabilidadInstance =  Contabilidad.get(params.id)
            contabilidadInstance.descripcion = params.descripcion
            contabilidadInstance.prefijo = params.prefijo.toUpperCase()
            contabilidadInstance.cuenta = params.cuenta.toInteger()

            if (!contabilidadInstance.save(flush: true)) {
                render "NO_Error al guardar los datos de la contabilidad"
                println("Error editar" + contabilidadInstance.errors)
            } else{
                render "OK_Datos de la contabilidad actualizados correctamente"
            }

        }else{
            contabilidadInstance = new Contabilidad()
            contabilidadInstance.institucion = session.empresa
            contabilidadInstance.descripcion = params.descripcion
            contabilidadInstance.prefijo = params.prefijo.toUpperCase()
            params.fechaInicio = new Date().parse("dd-MM-yyyy", params.fechaInicio_input)
            params.fechaCierre = new Date().parse("dd-MM-yyyy", params.fechaCierre_input)
            contabilidadInstance.fechaInicio = params.fechaInicio
            contabilidadInstance.fechaCierre = params.fechaCierre
            contabilidadInstance.presupuesto = params.fechaInicio
            contabilidadInstance.cuenta = params.cuenta.toInteger()


            if (!contabilidadInstance.save(flush: true)) {
                render "NO_Error al crear la contabilidad"
                println("Error" + contabilidadInstance.errors)
            } else{

                def mesInicio = params.fechaInicio_month.toInteger()
                def mesFin = params.fechaCierre_month.toInteger()

                if(mesFin == mesInicio){

                    def soloPeriodo = new Periodo()
                    soloPeriodo.contabilidad = contabilidadInstance
                    soloPeriodo.fechaInicio = contabilidadInstance.fechaInicio
                    soloPeriodo.fechaFin = contabilidadInstance.fechaCierre
                    soloPeriodo.numero = 1

                    if(!soloPeriodo.save(flush: true)){
                        println("error solo periodo " + soloPeriodo.errors)
                        render "NO_Error al crear la contabilidad"
                    }else{
                        render "OK_Contabilidad creada correctamente!"
                    }

                }else{


                    def inicioPrimer = contabilidadInstance.fechaInicio
                    def finPrimer = utilitarioService.getLastDayOfMonth(inicioPrimer)

                    def primerPeriodo = new Periodo()
                    primerPeriodo.contabilidad = contabilidadInstance
                    primerPeriodo.fechaInicio = inicioPrimer
                    primerPeriodo.fechaFin = finPrimer
                    primerPeriodo.numero = 1

                    if(!primerPeriodo.save(flush: true)){
                        println("error primer periodo " + primerPeriodo.errors)
                        errores += primerPeriodo.errors
                    }else{

                        def siguientePeriodo
                        def repe = mesFin - mesInicio

                        repe.times {
                            def ini = new Date().parse("dd-MM-yyyy", "01-" + ((mesInicio + it + 1).toString().padLeft(2, '0')) + "-" + contabilidadInstance.fechaInicio.format("yyyy"))
//                            println("ini " + ini)

                            def periodoInstance = new Periodo()
                            periodoInstance.contabilidad = contabilidadInstance
                            periodoInstance.fechaInicio = ini
                            periodoInstance.numero = it + 2


                            if(mesFin == (mesInicio + it + 1)){
                                periodoInstance.fechaFin = contabilidadInstance.fechaCierre
                            }else{
                                def fin = utilitarioService.getLastDayOfMonth(ini)
                                periodoInstance.fechaFin = fin
                            }

                            if (!periodoInstance.save(flush: true)) {
                                errores += periodoInstance.errors
                            }
                        }
                    }

                    println("errores " + errores)

                    if(errores == ''){
                        render "OK_Contabilidad creada correctamente"
                    }else{
                        render "NO_Error al crear la contabilidad"
                    }
                }
            }
        }

        //antiguo

//        def errores = ''
//
//        params.each { k, v ->
//            if (v != "date.struct" && v instanceof java.lang.String) {
//                params[k] = v.toUpperCase()
//            }
//        }

//        params.institucion = session.empresa
//
//        def contabilidadInstance = new Contabilidad()
//        if (params.id) {
//            contabilidadInstance = Contabilidad.get(params.id)
//            if (!contabilidadInstance) {
//                notFound_ajax()
//                return
//            }
//        } //update
//
//        if (params.anio) {
//            params.fechaInicio = new Date().parse("dd-MM-yyyy", '01-01-' + params.anio)
//            params.fechaCierre = new Date().parse("dd-MM-yyyy", '31-12-' + params.anio)
//        }
//
//
//        contabilidadInstance.properties = params
//        contabilidadInstance.presupuesto = contabilidadInstance.fechaInicio
//
//        if (!contabilidadInstance.save(flush: true)) {
//            def msg = "NO_No se pudo ${params.id ? 'actualizar' : 'crear'} Contabilidad."
//            msg += renderErrors(bean: contabilidadInstance)
//            render msg
//            return
//        }
//
//        if (Periodo.countByContabilidad(contabilidadInstance) == 0) {
//            12.times {
//                def ini = new Date().parse("dd-MM-yyyy", "01-" + ((it + 1).toString().padLeft(2, '0')) + "-" + contabilidadInstance.fechaInicio.format("yyyy"))
//                def fin = utilitarioService.getLastDayOfMonth(ini)
////                println("primero " + ini)
////                println("ultimo " + fin)
//                def periodoInstance = new Periodo()
//
//                periodoInstance.contabilidad = contabilidadInstance
//                periodoInstance.fechaInicio = ini
//                periodoInstance.fechaFin = fin
//                periodoInstance.numero = it + 1
//
//                if (!periodoInstance.save(flush: true)) {
//                   errores += periodoInstance.save()
//                }
//            }
//        }
//
////        println("texto errores " + errores)
//
//        if(errores == ''){
//            render "OK_${params.id ? 'Actualización' : 'Creación'} de Contabilidad exitosa."
//        }else{
//            render "NO_Error al grabar períodos"
//        }

//        render "OK_${params.id ? 'Actualización' : 'Creación'} de Contabilidad exitosa."
    } //save para grabar desde ajax

    def delete_ajax() {

//        println("params delete " + params)

        def contabilidadInstance = Contabilidad.get(params.id)
        def periodos = Periodo.findAllByContabilidad(contabilidadInstance)
        def procesos = Proceso.findAllByContabilidad(contabilidadInstance)
        def errores = ''

        if(procesos){
            render "NO_No se puede borrar esta contabilidad, ya tiene procesos asociados!"
        }else{

            periodos.each {p->
                try{
                    p.delete(flush: true)
                }catch (e){
                    errores += p.errors
                }
            }

//            println("errores " + errores)

            if(errores == ''){
                try{
                    contabilidadInstance.delete(flush: true)
                    render "OK_Contabilidad borrada correctamente!"
                }catch (e){
                    render "NO_Error al borrar la contabilidad"
                }
            }
        }


//        if (params.id) {
//            def contabilidadInstance = Contabilidad.get(params.id)
//            if (contabilidadInstance) {
//                try {
//                    contabilidadInstance.delete(flush: true)
//                    render "OK_Eliminación de Contabilidad exitosa."
//                } catch (e) {
//                    render "NO_No se pudo eliminar Contabilidad."
//                }
//            } else {
//                notFound_ajax()
//            }
//        } else {
//            notFound_ajax()
//        }
    } //delete para eliminar via ajax

    protected void notFound_ajax() {
        render "NO_No se encontró Contabilidad."
    } //notFound para ajax

    /* ******************** COPIADO HASTA AQUI ********************************************* */


    def index() {
        redirect(action: "list", params: params)
    }

//    def list() {
//        params.max = Math.min(params.max ? params.int('max') : 10, 100)
//        [contabilidadInstanceList: Contabilidad.list(params), contabilidadInstanceTotal: Contabilidad.count()]
//    }

    def create() {
        [contabilidadInstance: new Contabilidad(params)]
    }

    def save() {

        if (params.fechaInicio) {
            params.fechaInicio = new Date().parse("dd-MM-yyyy", params.fechaInicio)
        }
        if (params.fechaCierre) {
            params.fechaCierre = new Date().parse("dd-MM-yyyy", params.fechaCierre)
        }
        if (params.presupuesto) {
            params.presupuesto = new Date().parse("dd-MM-yyyy", params.presupuesto)
        }

        def contabilidadInstance = new Contabilidad(params)

        if (params.id) {
            contabilidadInstance = Contabilidad.get(params.id)
            contabilidadInstance.properties = params
        }

        if (!contabilidadInstance.save(flush: true)) {
            if (params.id) {
                render(view: "edit", model: [contabilidadInstance: contabilidadInstance])
            } else {
                render(view: "create", model: [contabilidadInstance: contabilidadInstance])
            }
            return
        }

        if (params.id) {
            flash.message = "Contabilidad actualizado"
            flash.clase = "success"
            flash.ico = "ss_accept"
        } else {
            flash.message = "Contabilidad creado"
            flash.clase = "success"
            flash.ico = "ss_accept"
        }

        12.times {
            def ini = new Date().parse("dd-MM-yyyy", "01-" + ((it + 1).toString().padLeft(2, '0')) + "-" + contabilidadInstance.fechaInicio.format("yyyy"))
            def fin = utilitarioService.getLastDayOfMonth(ini)
            def periodoInstance = new Periodo()

            if (periodoInstance.save(flush: true)) {

                periodoInstance.contabilidad = contabilidadInstance
                periodoInstance.fechaInicio = ini
                periodoInstance.fechaFin = fin
                periodoInstance.numero = it + 1
            } else {

                render "Error al grabar períodos"
            }
        }

        redirect(action: "show", id: contabilidadInstance.id)
    }

    def show() {
        def contabilidadInstance = Contabilidad.get(params.id)
        if (!contabilidadInstance) {
            flash.message = "No se encontró Contabilidad con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        [contabilidadInstance: contabilidadInstance]
    }

    def edit() {
        def contabilidadInstance = Contabilidad.get(params.id)
        if (!contabilidadInstance) {
            flash.message = "No se encontró Contabilidad con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        [contabilidadInstance: contabilidadInstance]
    }

    def delete() {
        def contabilidadInstance = Contabilidad.get(params.id)
        if (!contabilidadInstance) {
            flash.message = "No se encontró Contabilidad con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        try {
            contabilidadInstance.delete(flush: true)
            flash.message = "Contabilidad  con id " + params.id + " eliminado"
            flash.clase = "success"
            flash.ico = "ss_accept"
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = "No se pudo eliminar Contabilidad con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "show", id: params.id)
        }
    }

    def formPeriodo_ajax () {

    }


}
