package cratos

import org.springframework.dao.DataIntegrityViolationException

class TipoDocumentoProveedorController extends cratos.seguridad.Shield  {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

//    def list() {
//        params.max = Math.min(params.max ? params.int('max') : 10, 100)
//        [tipoDocumentoProveedorInstanceList: TipoDocumentoProveedor.list(params), tipoDocumentoProveedorInstanceTotal: TipoDocumentoProveedor.count()]
//    }

    def create() {
        [tipoDocumentoProveedorInstance: new TipoDocumentoProveedor(params)]
    }

    def save() {
        def tipoDocumentoProveedorInstance = new TipoDocumentoProveedor(params)

        if (params.id) {
            tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
            tipoDocumentoProveedorInstance.properties = params
        }

        if (!tipoDocumentoProveedorInstance.save(flush: true)) {
            if (params.id) {
                render(view: "edit", model: [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance])
            } else {
                render(view: "create", model: [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance])
            }
            return
        }

        if (params.id) {
            flash.message = "TipoDocumentoProveedor actualizado"
            flash.clase = "success"
            flash.ico = "ss_accept"
        } else {
            flash.message = "TipoDocumentoProveedor creado"
            flash.clase = "success"
            flash.ico = "ss_accept"
        }
        redirect(action: "show", id: tipoDocumentoProveedorInstance.id)
    }

    def show() {
        def tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
        if (!tipoDocumentoProveedorInstance) {
            flash.message = "No se encontró TipoDocumentoProveedor con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance]
    }

    def edit() {
        def tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
        if (!tipoDocumentoProveedorInstance) {
            flash.message = "No se encontró TipoDocumentoProveedor con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance]
    }

    def delete() {
        def tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
        if (!tipoDocumentoProveedorInstance) {
            flash.message = "No se encontró TipoDocumentoProveedor con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "list")
            return
        }

        try {
            tipoDocumentoProveedorInstance.delete(flush: true)
            flash.message = "TipoDocumentoProveedor  con id " + params.id + " eliminado"
            flash.clase = "success"
            flash.ico = "ss_accept"
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = "No se pudo eliminar TipoDocumentoProveedor con id " + params.id
            flash.clase = "error"
            flash.ico = "ss_delete"
            redirect(action: "show", id: params.id)
        }
    }

    /* ************************ COPIAR DESDE AQUI ****************************/

    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        def tipoDocumentoProveedorInstanceList = TipoDocumentoProveedor.list(params)
        def tipoDocumentoProveedorInstanceCount = TipoDocumentoProveedor.count()
        if (tipoDocumentoProveedorInstanceList.size() == 0 && params.offset && params.max) {
            params.offset = params.offset - params.max
        }
        tipoDocumentoProveedorInstanceList = TipoDocumentoProveedor.list(params)
        return [tipoDocumentoProveedorInstanceList: tipoDocumentoProveedorInstanceList, tipoDocumentoProveedorInstanceCount: tipoDocumentoProveedorInstanceCount]
    } //list

    def show_ajax() {


        if (params.id) {
            def tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
            if (!tipoDocumentoProveedorInstance) {
                notFound_ajax()
                return
            }
            return [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance]
        } else {
            notFound_ajax()
        }
    } //show para cargar con ajax en un dialog

    def form_ajax() {
        def tipoDocumentoProveedorInstance = new TipoDocumentoProveedor(params)
        if (params.id) {
            tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
            if (!tipoDocumentoProveedorInstance) {
                notFound_ajax()
                return
            }
        }
        return [tipoDocumentoProveedorInstance: tipoDocumentoProveedorInstance]
    } //form para cargar con ajax en un dialog

    def save_ajax() {

//        println("params:" + params)

//        params.each { k, v ->
//            if (v != "date.struct" && v instanceof java.lang.String) {
//                params[k] = v.toUpperCase()
//            }
//        }

        //nuevo

        def persona

        params.descripcion = params.descripcion.toUpperCase()

        //original
        def tipoDocumentoProveedorInstance = new TipoDocumentoProveedor()
        if (params.id) {
            tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
            tipoDocumentoProveedorInstance.properties = params
            if (!tipoDocumentoProveedorInstance) {
                notFound_ajax()
                return
            }
        }else {

            tipoDocumentoProveedorInstance = new TipoDocumentoProveedor()
            tipoDocumentoProveedorInstance.properties = params
//            tipodocumentoproveedorInstance.estado = '1'
//            tipoDocumentoProveedorInstance.empresa = session.empresa


        } //update


        if (!tipoDocumentoProveedorInstance.save(flush: true)) {
            def msg = "NO_No se pudo ${params.id ? 'actualizar' : 'crear'} Tipo de Documento de Proveedor."
            msg += renderErrors(bean: tipoDocumentoProveedorInstance)
            render msg
            return
        }
        render "OK_${params.id ? 'Actualización' : 'Creación'} de Tipo de Documento de Proveedor exitosa."
    } //save para grabar desde ajax



    def delete_ajax() {
        if (params.id) {
            def tipoDocumentoProveedorInstance = TipoDocumentoProveedor.get(params.id)
            if (tipoDocumentoProveedorInstance) {
                try {
                    tipoDocumentoProveedorInstance.delete(flush: true)
                    render "OK_Eliminación de Tipo de Documento de Proveedor exitosa."
                } catch (e) {
                    render "NO_No se pudo eliminar el Tipo de Documento de Proveedor."
                }
            } else {
                notFound_ajax()
            }
        } else {
            notFound_ajax()
        }
    } //delete para eliminar via ajax

    protected void notFound_ajax() {
        render "NO_No se encontró Tipo de Documento de Proveedor."
    } //notFound para ajax

}
