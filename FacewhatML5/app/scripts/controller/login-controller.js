/**
 * Created by chenyi on 2017-09-22.
 */
//$(document).ready(function () {
function bindLoginClick() {
    XoW.logger.d("bindLoginClick() " + XoW.utils.getCurrentDatetime());
    // alert();
    gblMgr = new XoW.GlobalManager();

    $("#loginIMSvr").bind("click", function () {
        //$('#loginSvr').bind('click', function() {
        alert("helloworld");
        var serviceURL = $('#service').val();
        var username = $('#username').val();
        var pass = $('#password').val();
        if (null == serviceURL || "" == serviceURL) {
            alert("请输入服务器名！");
            return false;
        }
        if (null == username || "" == username) {
            alert("请输入用户名！");
            return false;
        }
        if (null == pass || "" == pass) {
            alert("请输入密码！");
            return false;
        }
        gblMgr.connect(serviceURL, username, pass);
    });

    $('#login2').bind('click', function () {
        var serviceURL = $('#service').val();
        var username = $('#username').val();
        var pass = $('#password').val();
        if (null == serviceURL || "" == serviceURL) {
            alert("请输入服务器名！");
            return false;
        }
        if (null == username || "" == username) {
            alert("请输入用户名！");
            return false;
        }
        if (null == pass || "" == pass) {
            alert("请输入密码！");
            return false;
        }

        gblMgr.connect(serviceURL, username, pass);
    });
}


$(function(){
    XoW.logger.d("on document ready");

    //得到焦点
    $("#password").focus(function(){
        $("#left_hand").animate({
            left: "150",
            top: " -38"
        },{step: function(){
            if(parseInt($("#left_hand").css("left"))>140){
                $("#left_hand").attr("class","left_hand");
            }
        }}, 2000);
        $("#right_hand").animate({
            right: "-64",
            top: "-38px"
        },{step: function(){
            if(parseInt($("#right_hand").css("right"))> -70){
                $("#right_hand").attr("class","right_hand");
            }
        }}, 2000);
    });
    //失去焦点
    $("#password").blur(function(){
        $("#left_hand").attr("class","initial_left_hand");
        $("#left_hand").attr("style","left:100px;top:-12px;");
        $("#right_hand").attr("class","initial_right_hand");
        $("#right_hand").attr("style","right:-112px;top:-12px");
    });

    bindLoginClick();
});