
<%@ page import="cratos.ConceptoRetencionImpuestoRenta" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="main">
        <title>Lista de ConceptoRetencionImpuestoRentas</title>

        <script type="text/javascript" src="${resource(dir: 'js/jquery/plugins/contextMenu', file: 'jquery.contextMenu.js')}"></script>
        <link rel="stylesheet" href="${resource(dir: 'js/jquery/plugins/contextMenu', file: 'jquery.contextMenu.css')}" type="text/css">

        <script type="text/javascript" src="${resource(dir: 'js/jquery/plugins/jquery-validation-1.9.0', file: 'jquery.validate.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir: 'js/jquery/plugins/jquery-validation-1.9.0', file: 'messages_es.js')}"></script>

    </head>
    <body>
        <div class="ui-widget-content ui-corner-all cont">
            <div class="ui-widget-header ui-corner-all titulo">
                Lista de ConceptoRetencionImpuestoRentas
                <div class="fright">
                    <g:link action="create" class="btnNew miniButton">Nuevo</g:link>
                </div>
            </div>

            <div id="list-conceptoRetencionImpuestoRenta" class="content scaffold-list" role="main">
                <g:if test="${flash.message}">
                    <div class="message" role="status">${flash.message}</div>
                </g:if>
                <table id="tbl-conceptoRetencionImpuestoRenta">
                    <thead>
                        <tr>
                            
                            <g:sortableColumn property="codigo" title="${message(code: 'conceptoRIRBienes.codigo.label', default: 'Codigo')}" />
                            
                            <g:sortableColumn property="descripcion" title="${message(code: 'conceptoRIRBienes.descripcion.label', default: 'Descripcion')}" />
                            
                        </tr>
                    </thead>
                    <tbody id="tb-conceptoRetencionImpuestoRenta">
                        <g:each in="${conceptoRetencionImpuestoRentaInstanceList}" status="i" var="conceptoRetencionImpuestoRentaInstance">
                            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="${conceptoRetencionImpuestoRentaInstance.id}">
                                
                                <td>${fieldValue(bean: conceptoRetencionImpuestoRentaInstance, field: "codigo")}</td>
                                
                                <td>${fieldValue(bean: conceptoRetencionImpuestoRentaInstance, field: "descripcion")}</td>
                                
                            </tr>
                        </g:each>
                    </tbody>
                </table>
                <g:if test="${conceptoRetencionImpuestoRentaInstanceList.size() < conceptoRetencionImpuestoRentaInstanceTotal}">
                    <div class="pagination">
                        <g:paginate total="${conceptoRetencionImpuestoRentaInstanceTotal}"  prev="Ant." next="Sig." />
                    </div>
                </g:if>
            </div>
        </div>

        <ul id="menu-conceptoRetencionImpuestoRenta" class="contextMenu">
            <li class="show">
                <a href="#show">Ver</a>
            </li>
            <li class="edit">
                <a href="#edit">Editar</a>
            </li>
            <li class="delete">
                <a href="#delete">Eliminar</a>
            </li>
        </ul>

        <div id="dlg-conceptoRetencionImpuestoRenta"></div>

        <div id="dlgLoad" class="ui-helper-hidden" style="text-align:center;">
            Cargando.....Por favor espere......<br/><br/>
            <img src="${resource(dir: 'images', file: 'spinner64.gif')}" alt=""/>
        </div>

        <script type="text/javascript">
            function openDlg(url, id, cont, ajax, title, buttons) {
                if (ajax) {
                $("#dlgLoad").dialog("open");
                    $.ajax({
                        async   : false,
                        type    : "POST",
                        url     : url,
                        data    : {
                            id : id
                        },
                        success : function (msg) {
                            $("#dlg-conceptoRIRBienes").html(msg);
                        },
                        complete : function () {
                            $("#dlgLoad").dialog("close");
                        }
                    });
                    $("#dlg-conceptoRIRBienes").dialog("option", "width", 420);
                } else {
                $("#dlg-conceptoRIRBienes").html(cont);
                }
                $("#dlg-conceptoRIRBienes").dialog("option", "title", title);
                $("#dlg-conceptoRIRBienes").dialog("option", "buttons", buttons);
                $("#dlg-conceptoRIRBienes").dialog("open");
            }

            function submitForm() {
                if ($("#frm-conceptoRIRBienes").valid()) {
                    $("#dlgLoad").dialog("open");
                    var data = $("#frm-conceptoRIRBienes").serialize();
                    var url = $("#frm-conceptoRIRBienes").attr("action");

                    $.ajax({
                        type    : "POST",
                        url     : url,
                        data    : data,
                        success : function (msg) {
                            location.reload(true);
                        }
                    });
                }
            }

            $(function () {
                $("#dlgLoad").dialog({
                    modal         : true,
                    autoOpen      : false,
                    closeOnEscape : false,
                    draggable     : false,
                    resizable     : false,
                    zIndex        : 9000,
                    open          : function (event, ui) {
                        $(event.target).parent().find(".ui-dialog-titlebar-close").remove();
                    }
                });

                $("#dlg-conceptoRIRBienes").dialog({
                    modal    : true,
                    autoOpen : false,
                    width    : 420,
                    zIndex   : 1000,
                    position : ["center", 10]
                });

                $("th").hover(function () {
                    $(this).addClass("hover");
                    var i = $(this).index();
                    $("#tb-conceptoRIRBienes").find("tr").each(function () {
                        $(this).children().eq(i).addClass("hover");
                    });
                }, function () {
                    $(".hover").removeClass("hover");
                });

                $("#tb-conceptoRIRBienes").find("tr").hover(function () {
                    $(this).addClass("hover");
                }, function () {
                    $(".hover").removeClass("hover");
                });

                $(".btnNew").button({
                    icons : {
                        primary : "ui-icon-document"
                    }
                }).click(function () {
                            var id = $(this).attr("id");
                            var url = $(this).attr("href");
                            var title = "Crear ConceptoRetencionImpuestoRenta";
                            var buttons = {
                                "Guardar"  : function () {
                                    submitForm();
                                },
                                "Cancelar" : function () {
                                    $("#dlg-conceptoRIRBienes").dialog("close");
                                }
                            };
                            openDlg(url, id, "", true, title, buttons);
                            return false;
                        });

                $("#tb-conceptoRIRBienes").find("tr").contextMenu({
                            menu : "menu-conceptoRIRBienes"
                        },
                        function (action, el, pos) {
                            $("#dlg-conceptoRIRBienes").html("");
                            var id = $(el).attr("id");
                            var title, buttons, url, cont;
                            switch (action) {
                                case "edit":
                                    title = "Editar ConceptoRetencionImpuestoRenta";
                                    buttons = {
                                        "Guardar"  : function () {
                                            submitForm();
                                        },
                                        "Cancelar" : function () {
                                            $("#dlg-conceptoRIRBienes").dialog("close");
                                        }
                                    };
                                    url = "${createLink(action:'edit')}/" + id;
                                    break;
                                case "show":
                                    title = "Ver ConceptoRetencionImpuestoRenta";
                                    buttons = {
                                        "Aceptar" : function () {
                                            $("#dlg-conceptoRIRBienes").dialog("close");
                                        }
                                    };
                                    url = "${createLink(action:'show')}/" + id;
                                    break;
                                case "delete":
                                    title = "Eliminar ConceptoRetencionImpuestoRenta";
                                    buttons = {
                                        "Aceptar"  : function () {
                                            $("#dlgLoad").dialog("open");
                                            $.ajax({
                                                type    : "POST",
                                                url     : "${createLink(action:'delete')}",
                                                data    : {
                                                    id : id
                                                },
                                                success : function (msg) {
                                                    location.reload(true);
                                                }
                                            });
                                        },
                                        "Cancelar" : function () {
                                            $("#dlg-conceptoRIRBienes").dialog("close");
                                        }
                                    };
                                    cont = "<span style='font-size: 16px;'> Est&aacute; seguro de querer eliminar este ConceptoRetencionImpuestoRenta?";
                                    cont += "<br/>Esta acci&oacute;n es definitiva.</span>"
                                    $("#dlg-conceptoRIRBienes").dialog("option", "width", 360);
                                    break;
                            }
                            openDlg(url, id, cont, action != "delete", title, buttons);
                        });
            });
        </script>
    </body>
</html>
