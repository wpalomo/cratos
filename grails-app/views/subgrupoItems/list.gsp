
<%@ page import="cratos.inventario.SubgrupoItems" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Lista de SubgrupoItems</title>
</head>
<body>

<elm:flashMessage tipo="${flash.tipo}" clase="${flash.clase}">${flash.message}</elm:flashMessage>

<!-- botones -->
<div class="btn-toolbar toolbar">
    <div class="btn-group">
        <g:link controller="inicio" action="parametrosEmpresa" class="btn btn-warning btnRegresar">
            <i class="fa fa-chevron-left"></i> Parámetros
        </g:link>
        <g:link action="form" class="btn btn-info btnCrear">
            <i class="fa fa-file-o"></i> Nuevo Subgrupo
        </g:link>
    </div>
   </div>

<table class="table table-condensed table-bordered table-striped">
    <thead>
    <tr>
        <th>Empresa</th>
        <th>Grupo</th>
        <g:sortableColumn property="codigo" title="Codigo" />
        <g:sortableColumn property="descripcion" title="Descripcion" />
    </tr>
    </thead>
    <tbody>
    <g:if test="${session.perfil.nombre == 'Administrador'}">
        <g:each in="${subgrupoItemsInstanceList}" status="i" var="subgrupoItemsInstance">
            <g:if test="${subgrupoItemsInstance.empresa.id == session.empresa.id}">
                <tr data-id="${subgrupoItemsInstance.id}">
                    <td>${fieldValue(bean: subgrupoItemsInstance, field: "empresa")}</td>
                    <td>${fieldValue(bean: subgrupoItemsInstance, field: "grupo")}</td>
                    <td>${fieldValue(bean: subgrupoItemsInstance, field: "codigo")}</td>
                    <td>${fieldValue(bean: subgrupoItemsInstance, field: "descripcion")}</td>
                </tr>
            </g:if>
        </g:each>
    </g:if>
    <g:else>
        <g:each in="${subgrupoItemsInstanceList}" status="i" var="subgrupoItemsInstance">
            <tr data-id="${subgrupoItemsInstance.id}">
                <td>${fieldValue(bean: subgrupoItemsInstance, field: "empresa")}</td>
                <td>${fieldValue(bean: subgrupoItemsInstance, field: "grupo")}</td>
                <td>${fieldValue(bean: subgrupoItemsInstance, field: "codigo")}</td>
                <td>${fieldValue(bean: subgrupoItemsInstance, field: "descripcion")}</td>
            </tr>
        </g:each>
    </g:else>
    </tbody>
</table>
<elm:pagination total="${subgrupoItemsInstanceCount}" params="${params}"/>

<script type="text/javascript">
    var id = null;
    function submitForm() {
        var $form = $("#frmSubgrupoItems");
        var $btn = $("#dlgCreateEdit").find("#btnSave");
        if ($form.valid()) {
            $btn.replaceWith(spinner);
            $.ajax({
                type    : "POST",
                url     : '${createLink(action:'save_ajax')}',
                data    : $form.serialize(),
                success : function (msg) {
                    var parts = msg.split("_");
                    log(parts[1], parts[0] == "OK" ? "success" : "error"); // log(msg, type, title, hide)
                    if (parts[0] == "OK") {
                        location.reload(true);
                    } else {
                        spinner.replaceWith($btn);
                        return false;
                    }
                }
            });
        } else {
            return false;
        } //else
    }
    function deleteRow(itemId) {
        bootbox.dialog({
            title   : "Alerta",
            message : "<i class='fa fa-trash-o fa-3x pull-left text-danger text-shadow'></i><p>¿Está seguro que desea eliminar el Subgrupo de Items seleccionado? Esta acción no se puede deshacer.</p>",
            buttons : {
                cancelar : {
                    label     : "Cancelar",
                    className : "btn-primary",
                    callback  : function () {
                    }
                },
                eliminar : {
                    label     : "<i class='fa fa-trash-o'></i> Eliminar",
                    className : "btn-danger",
                    callback  : function () {
                        $.ajax({
                            type    : "POST",
                            url     : '${createLink(action:'delete_ajax')}',
                            data    : {
                                id : itemId
                            },
                            success : function (msg) {
                                var parts = msg.split("_");
                                log(parts[1], parts[0] == "OK" ? "success" : "error"); // log(msg, type, title, hide)
                                if (parts[0] == "OK") {
                                    location.reload(true);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    function createEditRow(id) {
        var title = id ? "Editar" : "Crear";
        var data = id ? { id: id } : {};
        $.ajax({
            type    : "POST",
            url     : "${createLink(action:'form_ajax')}",
            data    : data,
            success : function (msg) {
                var b = bootbox.dialog({
                    id      : "dlgCreateEdit",
                    title   : title + " Subgrupo de Items",
                    message : msg,
                    buttons : {
                        cancelar : {
                            label     : "Cancelar",
                            className : "btn-primary",
                            callback  : function () {
                            }
                        },
                        guardar  : {
                            id        : "btnSave",
                            label     : "<i class='fa fa-save'></i> Guardar",
                            className : "btn-success",
                            callback  : function () {
                                return submitForm();
                            } //callback
                        } //guardar
                    } //buttons
                }); //dialog
                setTimeout(function () {
                    b.find(".form-control").first().focus()
                }, 500);
            } //success
        }); //ajax
    } //createEdit

    $(function () {

        $(".btnCrear").click(function() {
            createEditRow();
            return false;
        });

        context.settings({
            onShow : function (e) {
                $("tr.success").removeClass("success");
                var $tr = $(e.target).parent();
                $tr.addClass("success");
                id = $tr.data("id");
            }
        });
        context.attach('tbody>tr', [
            {
                header : 'Acciones'
            },
            {
                text   : 'Ver',
                icon   : "<i class='fa fa-search'></i>",
                action : function (e) {
                    $("tr.success").removeClass("success");
                    e.preventDefault();
                    $.ajax({
                        type    : "POST",
                        url     : "${createLink(action:'show_ajax')}",
                        data    : {
                            id : id
                        },
                        success : function (msg) {
                            bootbox.dialog({
                                title   : "Ver Subgrupo de Items",
                                message : msg,
                                buttons : {
                                    ok : {
                                        label     : "Aceptar",
                                        className : "btn-primary",
                                        callback  : function () {
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            },
            {
                text   : 'Editar',
                icon   : "<i class='fa fa-pencil'></i>",
                action : function (e) {
                    $("tr.success").removeClass("success");
                    e.preventDefault();
                    createEditRow(id);
                }
            },
            {divider : true},
            {
                text   : 'Eliminar',
                icon   : "<i class='fa fa-trash-o'></i>",
                action : function (e) {
                    $("tr.success").removeClass("success");
                    e.preventDefault();
                    deleteRow(id);
                }
            }
        ]);
    });
</script>

</body>
</html>
