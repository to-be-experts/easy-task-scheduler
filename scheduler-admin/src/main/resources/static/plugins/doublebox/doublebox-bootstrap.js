(function($, window, document, undefined) {
    var pluginName = "bootstrapDualListbox",
        defaults = {
            bootstrap2Compatible: false,
            filterTextClear: "show all",
            filterPlaceHolder: "任务ID/任务名",
            moveSelectedLabel: "Move selected",
            removeSelectedLabel: "Remove selected",
            moveOnSelect: true,
            preserveSelectionOnMove: false,
            selectedListLabel: false,
            nonSelectedListLabel: false,
            helperSelectNamePostfix: "_helper",
            selectorMinimalHeight: 100,
            showFilterInputs: true,
            nonSelectedFilter: "",
            selectedFilter: "",
            filterOnValues: false,
            doubleMove: false
        },
        isBuggyAndroid = /android/i.test(navigator.userAgent.toLowerCase());
    function BootstrapDualListbox(element, options) {
        this.element = $(element);
        this.settings = $.extend({},
            defaults, options);
        this._defaults = defaults;
        this._name = pluginName;
        this.init()
    }
    function triggerChangeEvent(dualListbox) {
        dualListbox.element.trigger("change")
    }
    function updateSelectionStates(dualListbox) {
        dualListbox.element.find("option").each(function(index, item) {
            var $item = $(item);
            if (typeof($item.data("original-index")) === "undefined") {
                $item.data("original-index", dualListbox.elementCount++)
            }
            if (typeof($item.data("_selected")) === "undefined") {
                $item.data("_selected", false)
            }
        })
    }
    function changeSelectionState(dualListbox, original_index, selected) {
        dualListbox.element.find("option").each(function(index, item) {
            var $item = $(item);
            if ($item.data("original-index") === original_index) {
                $item.prop("selected", selected)
            }
        })
    }
    function formatString(s, args) {
        return s.replace(/\{(\d+)\}/g,
            function(match, number) {
                return typeof args[number] !== "undefined" ? args[number] : match
            })
    }
    function refreshInfo(dualListbox) {
        if (!dualListbox.settings.infoText) {
            return
        }
        var visible1 = dualListbox.elements.select1.find("option").length,
            visible2 = dualListbox.elements.select2.find("option").length,
            all1 = dualListbox.element.find("option").length - dualListbox.selectedElements,
            all2 = dualListbox.selectedElements,
            content = "";
        if (all1 === 0) {
            content = dualListbox.settings.infoTextEmpty
        } else {
            if (visible1 === all1) {
                content = formatString(dualListbox.settings.infoText, [visible1, all1])
            } else {
                content = formatString(dualListbox.settings.infoTextFiltered, [visible1, all1])
            }
        }
        dualListbox.elements.info1.html(content);
        dualListbox.elements.box1.toggleClass("filtered", !(visible1 === all1 || all1 === 0));
        if (all2 === 0) {
            content = dualListbox.settings.infoTextEmpty
        } else {
            if (visible2 === all2) {
                content = formatString(dualListbox.settings.infoText, [visible2, all2])
            } else {
                content = formatString(dualListbox.settings.infoTextFiltered, [visible2, all2])
            }
        }
        dualListbox.elements.info2.html(content);
        dualListbox.elements.box2.toggleClass("filtered", !(visible2 === all2 || all2 === 0))
    }
    function refreshSelects(dualListbox) {
        dualListbox.selectedElements = 0;
        dualListbox.elements.select1.empty();
        dualListbox.elements.select2.empty();
        dualListbox.element.find("option").each(function(index, item) {
            var $item = $(item);
            if ($item.prop("selected")) {
                dualListbox.selectedElements++;
                dualListbox.elements.select2.append($item.clone(true).prop("selected", $item.data("_selected")))
            } else {
                dualListbox.elements.select1.append($item.clone(true).prop("selected", $item.data("_selected")))
            }
        });
        if (dualListbox.settings.showFilterInputs) {
            filter(dualListbox, 1);
            filter(dualListbox, 2)
        }
        refreshInfo(dualListbox)
    }
    function filter(dualListbox, selectIndex) {
        if (!dualListbox.settings.showFilterInputs) {
            return
        }
        saveSelections(dualListbox, selectIndex);
        dualListbox.elements["select" + selectIndex].empty().scrollTop(0);
        var regex = new RegExp($.trim(dualListbox.elements["filterInput" + selectIndex].val()), "gi"),
            options = dualListbox.element;
        if (selectIndex === 1) {
            options = options.find("option").not(":selected")
        } else {
            options = options.find("option:selected")
        }
        options.each(function(index, item) {
            var $item = $(item),
                isFiltered = true;
            if (item.text.match(regex) || (dualListbox.settings.filterOnValues && $item.attr("value").match(regex))) {
                isFiltered = false;
                dualListbox.elements["select" + selectIndex].append($item.clone(true).prop("selected", $item.data("_selected")))
            }
            dualListbox.element.find("option").eq($item.data("original-index")).data("filtered" + selectIndex, isFiltered)
        });
        refreshInfo(dualListbox)
    }
    function saveSelections(dualListbox, selectIndex) {
        dualListbox.elements["select" + selectIndex].find("option").each(function(index, item) {
            var $item = $(item);
            dualListbox.element.find("option").eq($item.data("original-index")).data("_selected", $item.prop("selected"))
        })
    }
    function sortOptions(select) {
        select.find("option").sort(function(a, b) {
            return ($(a).data("original-index") > $(b).data("original-index")) ? 1 : -1
        }).appendTo(select)
    }
    function clearSelections(dualListbox) {
        dualListbox.elements.select1.find("option").each(function() {
            dualListbox.element.find("option").data("_selected", false)
        })
    }
    function move(dualListbox) {
        if (dualListbox.settings.preserveSelectionOnMove === "all" && !dualListbox.settings.moveOnSelect) {
            saveSelections(dualListbox, 1);
            saveSelections(dualListbox, 2)
        } else {
            if (dualListbox.settings.preserveSelectionOnMove === "moved" && !dualListbox.settings.moveOnSelect) {
                saveSelections(dualListbox, 1)
            }
        }
        dualListbox.elements.select1.find("option:selected").each(function(index, item) {
            var $item = $(item);
            if (!$item.data("filtered1")) {
                changeSelectionState(dualListbox, $item.data("original-index"), true)
            }
        });
        refreshSelects(dualListbox);
        triggerChangeEvent(dualListbox);
        sortOptions(dualListbox.elements.select2)
    }
    function remove(dualListbox) {
        if (dualListbox.settings.preserveSelectionOnMove === "all" && !dualListbox.settings.moveOnSelect) {
            saveSelections(dualListbox, 1);
            saveSelections(dualListbox, 2)
        } else {
            if (dualListbox.settings.preserveSelectionOnMove === "moved" && !dualListbox.settings.moveOnSelect) {
                saveSelections(dualListbox, 2)
            }
        }
        dualListbox.elements.select2.find("option:selected").each(function(index, item) {
            var $item = $(item);
            if (!$item.data("filtered2")) {
                changeSelectionState(dualListbox, $item.data("original-index"), false)
            }
        });
        refreshSelects(dualListbox);
        triggerChangeEvent(dualListbox);
        sortOptions(dualListbox.elements.select1)
    }
    function upSort(dualListbox) {
        dualListbox.elements.select2.find("option:selected").each(function(index, item) {
            var $item = $(item);
            var $target = $item.prev();
            $item.insertBefore($target)
        })
    }
    function downSort(dualListbox) {
        dualListbox.elements.select2.find("option:selected").each(function(index, item) {
            var $item = $(item);
            var $target = $item.next();
            $item.insertAfter($target)
        })
    }
    function bindEvents(dualListbox) {
        dualListbox.elements.form.submit(function(e) {
            if (dualListbox.elements.filterInput1.is(":focus")) {
                e.preventDefault();
                dualListbox.elements.filterInput1.focusout()
            } else {
                if (dualListbox.elements.filterInput2.is(":focus")) {
                    e.preventDefault();
                    dualListbox.elements.filterInput2.focusout()
                }
            }
        });
        dualListbox.element.on("bootstrapDualListbox.refresh",
            function(e, mustClearSelections) {
                dualListbox.refresh(mustClearSelections)
            });
        dualListbox.elements.filterClear1.on("click",
            function() {
                dualListbox.setNonSelectedFilter("", true)
            });
        dualListbox.elements.filterClear2.on("click",
            function() {
                dualListbox.setSelectedFilter("", true)
            });
        dualListbox.elements.moveButton.on("click",
            function() {
                move(dualListbox)
            });
        dualListbox.elements.removeButton.on("click",
            function() {
                remove(dualListbox)
            });
        dualListbox.elements.upButton.on("click",
            function() {
                upSort(dualListbox)
            });
        dualListbox.elements.downButton.on("click",
            function() {
                downSort(dualListbox)
            });
        dualListbox.elements.filterInput1.on("change keyup",
            function() {
                filter(dualListbox, 1)
            });
        dualListbox.elements.filterInput2.on("change keyup",
            function() {
                filter(dualListbox, 2)
            });
        dualListbox.elements.filterInput2.on("change keyup",
            function() {
                filter(dualListbox, 2)
            })
    }
    BootstrapDualListbox.prototype = {
        init: function() {
            this.container = $("" + '<div class="bootstrap-duallistbox-container">' + ' <div class="box1">' + "   <label></label>" + '   <span class="info-container">' + '     <span class="info"></span>' + '     <button type="button" class="btn clear1 pull-right"></button>' + "   </span>" + '   <input class="filter form-control ue-form" type="text">' + '   <select multiple="multiple"></select>' + " </div>" + ' <div class="btn-box">' + '     <button type="button" class="btn db-btn move">' + "       <i></i>" + "     </button>" + '     <p class="clearfix" style="margin-bottom:20px"></p>' + '     <button type="button" class="btn db-btn remove">' + "       <i></i>" + "     </button>" + "       <i></i>" + "     </button>" + " </div>" + ' <div class="box2">' + "   <label></label>" + '   <span class="info-container">' + '     <span class="info"></span>' + '     <button type="button" class="btn clear2 pull-right"></button>' + "   </span>" + '   <input class="filter form-control ue-form" type="text">' + '   <select multiple="multiple"></select>' + " </div> </div>").insertBefore(this.element);
            this.elements = {
                originalSelect: this.element,
                box1: $(".box1", this.container),
                box2: $(".box2", this.container),
                filterInput1: $(".box1 .filter", this.container),
                filterInput2: $(".box2 .filter", this.container),
                filterClear1: $(".box1 .clear1", this.container),
                filterClear2: $(".box2 .clear2", this.container),
                label1: $(".box1 > label", this.container),
                label2: $(".box2 > label", this.container),
                info1: $(".box1 .info", this.container),
                info2: $(".box2 .info", this.container),
                select1: $(".box1 select", this.container),
                select2: $(".box2 select", this.container),
                moveButton: $(".btn-box .move", this.container),
                removeButton: $(".btn-box .remove", this.container),
                upButton: $(".settingUp-btns .upBtn", this.container),
                downButton: $(".settingUp-btns .downBtn", this.container),
                form: $($(".box1 .filter", this.container)[0].form)
            };
            this.originalSelectName = this.element.attr("name") || "";
            var select1Id = "bootstrap-duallistbox-nonselected-list_" + this.originalSelectName,
                select2Id = "bootstrap-duallistbox-selected-list_" + this.originalSelectName;
            this.elements.select1.attr("id", select1Id);
            this.elements.select2.attr("id", select2Id);
            this.elements.label1.attr("for", select1Id);
            this.elements.label2.attr("for", select2Id);
            this.selectedElements = 0;
            this.elementCount = 0;
            this.setBootstrap2Compatible(this.settings.bootstrap2Compatible);
            this.setFilterTextClear(this.settings.filterTextClear);
            this.setFilterPlaceHolder(this.settings.filterPlaceHolder);
            this.setMoveSelectedLabel(this.settings.moveSelectedLabel);
            this.setRemoveSelectedLabel(this.settings.removeSelectedLabel);
            this.setMoveOnSelect(this.settings.moveOnSelect);
            this.setPreserveSelectionOnMove(this.settings.preserveSelectionOnMove);
            this.setSelectedListLabel(this.settings.selectedListLabel);
            this.setNonSelectedListLabel(this.settings.nonSelectedListLabel);
            this.setHelperSelectNamePostfix(this.settings.helperSelectNamePostfix);
            this.setSelectOrMinimalHeight(this.settings.selectorMinimalHeight);
            this.setDoubleMove(this.settings.doubleMove);
            updateSelectionStates(this);
            this.setShowFilterInputs(this.settings.showFilterInputs);
            this.setNonSelectedFilter(this.settings.nonSelectedFilter);
            this.setSelectedFilter(this.settings.selectedFilter);
            this.setInfoText(this.settings.infoText);
            this.setInfoTextFiltered(this.settings.infoTextFiltered);
            this.setInfoTextEmpty(this.settings.infoTextEmpty);
            this.setFilterOnValues(this.settings.filterOnValues);
            this.element.hide();
            bindEvents(this);
            refreshSelects(this);
            return this.element
        },
        setBootstrap2Compatible: function(value, refresh) {
            this.settings.bootstrap2Compatible = value;
            if (value) {
                this.container.removeClass("row").addClass("row-fluid bs2compatible");
                this.container.find(".box1, .box2").removeClass("col-md-5");
                this.container.find(".btn-box").removeClass("col-md-1");
                this.container.find(".clear1, .clear2").removeClass("btn-default btn-xs").addClass("btn-mini");
                this.container.find("input, select").removeClass("form-control");
                this.container.find(".btn").removeClass("btn-default");
                this.container.find(".moveall > i, .move > i").removeClass("glyphicon glyphicon-arrow-right").addClass("icon-arrow-right");
                this.container.find(".removeall > i, .remove > i").removeClass("glyphicon glyphicon-arrow-left").addClass("icon-arrow-left");
                this.container.find(".upBtn > i").removeClass("glyphicon glyphicon-arrow-up").addClass("icon-arrow-up");
                this.container.find(".downBtn > i").removeClass("glyphicon glyphicon-arrow-down").addClass("icon-arrow-down")
            } else {
                this.container.removeClass("row-fluid bs2compatible").addClass("row");
                this.container.find(".box1, .box2").addClass("col-md-5");
                this.container.find(".btn-box").addClass("col-md-1");
                this.container.find(".clear1, .clear2").removeClass("btn-mini").addClass("btn-default btn-xs");
                this.container.find("input, select").addClass("form-control");
                this.container.find(".btn").addClass("btn-default");
                this.container.find(".moveall > i, .move > i").removeClass("icon-arrow-right").addClass("glyphicon glyphicon-arrow-right");
                this.container.find(".removeall > i, .remove > i").removeClass("icon-arrow-left").addClass("glyphicon glyphicon-arrow-left");
                this.container.find(".upBtn > i").removeClass("icon-arrow-up").addClass("glyphicon glyphicon-arrow-up");
                this.container.find(".downBtn > i").removeClass("icon-arrow-down").addClass("glyphicon glyphicon-arrow-down")
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setFilterTextClear: function(value, refresh) {
            this.settings.filterTextClear = value;
            this.elements.filterClear1.html(value);
            this.elements.filterClear2.html(value);
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setFilterPlaceHolder: function(value, refresh) {
            this.settings.filterPlaceHolder = value;
            this.elements.filterInput1.attr("placeholder", value);
            this.elements.filterInput2.attr("placeholder", value);
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setMoveSelectedLabel: function(value, refresh) {
            this.settings.moveSelectedLabel = value;
            this.elements.moveButton.attr("title", value);
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setRemoveSelectedLabel: function(value, refresh) {
            this.settings.removeSelectedLabel = value;
            this.elements.removeButton.attr("title", value);
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setMoveOnSelect: function(value, refresh) {
            if (isBuggyAndroid) {
                value = true
            }
            this.settings.moveOnSelect = value;
            if (this.settings.moveOnSelect) {
                this.container.addClass("moveonselect");
                var self = this;
                this.elements.select1.on("change",
                    function() {
                        move(self)
                    });
                this.elements.select2.on("change",
                    function() {
                        remove(self)
                    })
            } else {
                this.container.removeClass("moveonselect");
                this.elements.select1.off("change");
                this.elements.select2.off("change")
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setDoubleMove: function(value, refresh) {
            if (isBuggyAndroid) {
                value = false
            }
            this.settings.doubleMove = value;
            if (this.settings.doubleMove) {
                var self = this;
                this.elements.select1.on("dblclick",
                    function() {
                        move(self)
                    });
                this.elements.select2.on("dblclick",
                    function() {
                        remove(self)
                    })
            } else {
                this.elements.select1.off("dblclick");
                this.elements.select2.off("dblclick")
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setPreserveSelectionOnMove: function(value, refresh) {
            if (isBuggyAndroid) {
                value = false
            }
            this.settings.preserveSelectionOnMove = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setSelectedListLabel: function(value, refresh) {
            this.settings.selectedListLabel = value;
            if (value) {
                this.elements.label2.show().html(value)
            } else {
                this.elements.label2.hide().html(value)
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setNonSelectedListLabel: function(value, refresh) {
            this.settings.nonSelectedListLabel = value;
            if (value) {
                this.elements.label1.show().html(value)
            } else {
                this.elements.label1.hide().html(value)
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setHelperSelectNamePostfix: function(value, refresh) {
            this.settings.helperSelectNamePostfix = value;
            if (value) {
                this.elements.select1.attr("name", this.originalSelectName + value + "1");
                this.elements.select2.attr("name", this.originalSelectName + value + "2")
            } else {
                this.elements.select1.removeAttr("name");
                this.elements.select2.removeAttr("name")
            }
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setSelectOrMinimalHeight: function(value, refresh) {
            this.settings.selectorMinimalHeight = value;
            var height = this.element.height();
            if (this.element.height() < value) {
                height = value
            }
            this.elements.select1.height(height);
            this.elements.select2.height(height);
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setShowFilterInputs: function(value, refresh) {
            if (!value) {
                this.setNonSelectedFilter("");
                this.setSelectedFilter("");
                refreshSelects(this);
                this.elements.filterInput1.hide();
                this.elements.filterInput2.hide()
            } else {
                this.elements.filterInput1.show();
                this.elements.filterInput2.show()
            }
            this.settings.showFilterInputs = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setNonSelectedFilter: function(value, refresh) {
            if (this.settings.showFilterInputs) {
                this.settings.nonSelectedFilter = value;
                this.elements.filterInput1.val(value);
                if (refresh) {
                    refreshSelects(this)
                }
                return this.element
            }
        },
        setSelectedFilter: function(value, refresh) {
            if (this.settings.showFilterInputs) {
                this.settings.selectedFilter = value;
                this.elements.filterInput2.val(value);
                if (refresh) {
                    refreshSelects(this)
                }
                return this.element
            }
        },
        setInfoText: function(value, refresh) {
            this.settings.infoText = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setInfoTextFiltered: function(value, refresh) {
            this.settings.infoTextFiltered = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setInfoTextEmpty: function(value, refresh) {
            this.settings.infoTextEmpty = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        setFilterOnValues: function(value, refresh) {
            this.settings.filterOnValues = value;
            if (refresh) {
                refreshSelects(this)
            }
            return this.element
        },
        getContainer: function() {
            return this.container
        },
        refresh: function(mustClearSelections) {
            updateSelectionStates(this);
            if (!mustClearSelections) {
                saveSelections(this, 1);
                saveSelections(this, 2)
            } else {
                clearSelections(this)
            }
            refreshSelects(this)
        },
        destroy: function() {
            this.container.remove();
            this.element.show();
            $.data(this, "plugin_" + pluginName, null);
            return this.element
        }
    };
    $.fn[pluginName] = function(options) {
        var args = arguments;
        if (options === undefined || typeof options === "object") {
            return this.each(function() {
                if (!$(this).is("select")) {
                    $(this).find("select").each(function(index, item) {
                        $(item).bootstrapDualListbox(options)
                    })
                } else {
                    if (!$.data(this, "plugin_" + pluginName)) {
                        $.data(this, "plugin_" + pluginName, new BootstrapDualListbox(this, options))
                    }
                }
            })
        } else {
            if (typeof options === "string" && options[0] !== "_" && options !== "init") {
                var returns;
                this.each(function() {
                    var instance = $.data(this, "plugin_" + pluginName);
                    if (instance instanceof BootstrapDualListbox && typeof instance[options] === "function") {
                        returns = instance[options].apply(instance, Array.prototype.slice.call(args, 1))
                    }
                });
                return returns !== undefined ? returns: this
            }
        }
    }
})(jQuery, window, document); (function(root, factory) {
    if (typeof exports === "object") {
        module.exports = factory(root, require("jquery"))
    } else {
        if (typeof define === "function" && define.amd) {
            define(["jquery"],
                function(jQuery) {
                    return factory(root, jQuery)
                })
        } else {
            factory(root, root.jQuery)
        }
    }
} (this,
    function(window, $, undefined) {
        $.fn.doublebox = function(options) {
            var box = this.bootstrapDualListbox(options);
            var items = "";
            box.selectElement = function() {
                if (options.nonSelectedList != null) {
                    for (var i in options.nonSelectedList) {
                        if (options.nonSelectedList.hasOwnProperty(i)) {
                            var optionText = options.nonSelectedList[i][options.optionText];
                            items += "<option value='" + options.nonSelectedList[i][options.optionValue] + "' title='"+optionText+"'>" + optionText + "</option>"
                        }
                    }
                }
                if (options.selectedList != null) {
                    for (var i in options.selectedList) {
                        if (options.selectedList.hasOwnProperty(i)) {
                            var optionText = options.selectedList[i][options.optionText];
                            items += "<option value='" + options.selectedList[i][options.optionValue] + "' selected title='"+optionText+"'>" + optionText + "</option>"
                        }
                    }
                }
                box.append(items);
                box.bootstrapDualListbox("refresh")
            };
            box.getSelectedOptions = function() {
                var items = $("#bootstrap-duallistbox-selected-list_doublebox>option").map(function() {
                    return $(this).val()
                }).get().join(",");
                return items
            };
            box.selectElement();
            return box
        }
    }));