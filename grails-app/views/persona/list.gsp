<%@ page import="cratos.seguridad.Persona" %>
<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main">
        <title>Usuarios</title>
    </head>

    <body>

        <elm:flashMessage tipo="${flash.tipo}" clase="${flash.clase}">${flash.message}</elm:flashMessage>

        <!-- botones -->
        <div class="btn-toolbar toolbar">
            <div class="btn-group">
                <g:link action="form" class="btn btn-info btnCrear">
                    <i class="fa fa-file-o"></i> Nuevo Usuario
                </g:link>
            </div>

            %{--<div class="btn-group pull-right col-md-3">--}%
                %{--<div class="input-group">--}%
                    %{--<input type="text" class="form-control" placeholder="Buscar">--}%
                    %{--<span class="input-group-btn">--}%
                        %{--<a href="#" class="btn btn-default" type="button">--}%
                            %{--<i class="fa fa-search"></i>&nbsp;--}%
                        %{--</a>--}%
                    %{--</span>--}%
                %{--</div><!-- /input-group -->--}%
            %{--</div>--}%
        </div>

    <g:if test="${session.usuario.login == 'admin'}">
        <h1 style="text-align: center">Usuarios</h1>
    </g:if>
    <g:else>
        <h1 style="text-align: center">${session.empresa.nombre}</h1>
    </g:else>


        <div class="vertical-container vertical-container-list">
            <p class="css-vertical-text">Lista de Usuarios</p>

            <div class="linea"></div>
            <table class="table table-condensed table-bordered table-striped table-hover">
                <thead>
                    <tr>
                        <th>Empresa</th>
                        <th>Cédula</th>
                        <th>Nombre</th>
                        <th>Apellido</th>
                        <th>Login</th>
                        <th>Activo</th>
                        <th width="190">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${personaInstanceList}" status="i" var="personaInstance">
                        <tr data-id="${personaInstance.id}">
                            <td style="background-color: #dddddd">${personaInstance?.empresa}</td>
                            <td style="text-align: center">${personaInstance.cedula}</td>
                            <td>${personaInstance.nombre}</td>
                            <td>${personaInstance.apellido}</td>
                            <td style="color: #00b3ff">${personaInstance.login}</td>
                            <td style="text-align: center; color: ${personaInstance.activo == 1 ? '#00aa00' : ' #ff0f24'}"><g:formatBoolean boolean="${personaInstance.activo == 1}" true="SI" false="NO"/></td>
                            <td>
                                <a href="#" data-id="${personaInstance.id}" class="btn btn-info btn-sm btn-show btn-ajax" title="Ver">
                                    <i class="fa fa-laptop"></i>
                                </a>
                                <a href="#" data-id="${personaInstance.id}" class="btn btn-success btn-sm btn-edit btn-ajax" title="Editar">
                                    <i class="fa fa-pencil"></i>
                                </a>
                                <g:link action="perfiles" id="${personaInstance.id}" class="btn btn-primary btn-sm btn-ajax" title="Perfiles">
                                    <i class="fa fa-users"></i>
                                </g:link>
                                <a href="#" data-id="${personaInstance.id}" class="btn btn-warning btn-pass btn-sm" title="Reiniciar password">
                                    <i class="fa fa-refresh"></i>
                                </a>
                                <a href="#" data-id="${personaInstance.id}" class="btn btn-danger btn-delete btn-sm" title="Eliminar">
                                    <i class="fa fa-trash-o"></i>
                                </a>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <elm:pagination total="${personaInstanceCount}" params="${params}"/>

        <script type="text/javascript">
            var id = null;
            function submitForm() {
                var $form = $("#frmPersona");
                var $btn = $("#dlgCreateEdit").find("#btnSave");
                if ($form.valid()) {
                    $btn.replaceWith(spinner);
                    openLoader("Grabando");
                    $.ajax({
                        type    : "POST",
                        url     : $form.attr("action"),
                        data    : $form.serialize(),
                        success : function (msg) {
                            var parts = msg.split("_");
                            log(parts[1], parts[0] == "OK" ? "success" : "error"); // log(msg, type, title, hide)
                            if (parts[0] == "OK") {
                                location.reload(true);
                            } else {
                                closeLoader();
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
                    message : "<i class='fa fa-trash-o fa-3x pull-left text-danger text-shadow'></i><p>¿Está seguro que desea eliminar el Usuario seleccionado? Esta acción no se puede deshacer.</p>",
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
                                openLoader("Eliminando");
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
                                        } else {
                                            closeLoader();
                                            spinner.replaceWith($btn);
                                            return false;
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
                var data = id ? { id : id } : {};
                $.ajax({
                    type    : "POST",
                    url     : "${createLink(action:'form_ajax')}",
                    data    : data,
                    success : function (msg) {
                        var b = bootbox.dialog({
                            id      : "dlgCreateEdit",
                            class   : "long",
                            title   : title + " Usuario",
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
                            b.find(".form-control").not(".datepicker").first().focus()
                        }, 500);
                    } //success
                }); //ajax
            } //createEdit

            $(function () {

                $(".btnCrear").click(function () {
                    createEditRow();
                    return false;
                });

                $(".btn-show").click(function () {
                    var id = $(this).data("id");
                    $.ajax({
                        type    : "POST",
                        url     : "${createLink(action:'show_ajax')}",
                        data    : {
                            id : id
                        },
                        success : function (msg) {
                            bootbox.dialog({
                                title   : "Ver Persona",
                                class   : "long",
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
                });
                $(".btn-edit").click(function () {
                    var id = $(this).data("id");
                    createEditRow(id);
                });
                $(".btn-pass").click(function () {
                    var id = $(this).data("id");
                    bootbox.dialog({
                        title   : "Alerta",
                        message : "<i class='fa fa-warning fa-3x pull-left text-danger text-shadow'></i><p>¿Está seguro que desea reiniciar el password del usuario?<br/>El password será el número de cédula y no se puede deshacer.</p>",
                        buttons : {
                            cancelar : {
                                label     : "Cancelar",
                                className : "btn-primary",
                                callback  : function () {
                                }
                            },
                            cambiar  : {
                                label     : "<i class='fa fa-refresh'></i> Reiniciar",
                                className : "btn-warning",
                                callback  : function () {
                                    openLoader("Modificando");
                                    $.ajax({
                                        type    : "POST",
                                        url     : '${createLink(action:'reset_pass_ajax')}',
                                        data    : {
                                            id : id
                                        },
                                        success : function (msg) {
                                            var parts = msg.split("_");
                                            log(parts[1], parts[0] == "OK" ? "success" : "error"); // log(msg, type, title, hide)
                                            if (parts[0] == "OK") {
                                                location.reload(true);
                                            } else {
                                                closeLoader();
                                                spinner.replaceWith($btn);
                                                return false;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                });
                $(".btn-delete").click(function () {
                    var id = $(this).data("id");
                    deleteRow(id);
                });

            });
        </script>

    </body>
</html>
