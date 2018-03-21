


(function ($) {

    var Switch = Switch || {};

    TM.Switch = Switch;

    Switch.createSwitch = Switch.createSwitch || {};

    Switch.createSwitch = $.extend({
        createSwitchForm:function(text, hideLable){
            var form=$('<form method="get" action="Switch"></form>');
            var ul=$('<ul style="list-style:none;"></ul>');
            ul.append('<li ><label class="tip-label" for="auto_valuation"></label><input type="checkbox" name="auto_valuation" data-on="ON" data-off="OFF" /></li>');
            form.append(ul);
            var label = form.find('label');
            if(hideLable){
                label.hide();
            }else{
                label.text(text);
            }
            return form;
        }
    },Switch.createSwitch);

    Switch.SetEvent = Switch.SetEvent || {};

    Switch.SetEvent = $.extend({
        setEvent:function(){
            $('.tzCheckBox').click(function(){
            });
        }
    }, Switch.SetEvent);

    $.fn.tzCheckbox = function (options) {
        // Default On / Off labels:

        options = $.extend({
            text:'自动评价',
            labels:['已开启', '已关闭'],
            doChange:function () {
                return true;
            },
            isOn:true
        }, options);

        return this.each(function () {
            var originalCheckBox = $(this),
                labels = [];


            labels = options.labels;

//            originalCheckBox.find('label').html(options.text);
            // Creating the new checkbox markup:
            var checkBox = $('<span>' + '<span class="tzCBContent">' + labels[options.isOn ? 0 : 1] +
            '</span><span class="tzCBPart"></span>' +  '</span>');

            checkBox.addClass('tzCheckBox');
            if(options.isOn){
                checkBox.addClass('checked');
            }
//            checkBox.append();

            // Inserting the new checkbox, and hiding the original:
            checkBox.insertAfter(originalCheckBox.hide());

            checkBox.click(function () {
                var isChecked = checkBox.hasClass('checked');

                if (options.doChange(isChecked)) {
                    checkBox.toggleClass('checked');
                    // Synchronizing the original checkbox:
                    originalCheckBox.attr('checked', isChecked);
                    checkBox.find('.tzCBContent').html(labels[isChecked ? 1 : 0]);
                }
            });

            // Listening for changes on the original and affecting the new one:
            originalCheckBox.bind('change', function () {
                checkBox.click();
            });
        });
    };
})(jQuery);


