if (document.images) {
    image1on = new Image();
    image1on.src = "http://img04.taobaocdn.com/imgextra/i4/22902351/T2qnDdXllXXXXXXXXX_!!22902351.png";

    image1off = new Image();
    image1off.src = "http://img01.taobaocdn.com/imgextra/i1/22902351/T2INBQXXBfXXXXXXXX_!!22902351.png";

}

function changeImages() {
    if (document.images) {
        for (var i=0; i<changeImages.arguments.length; i+=2) {
            document[changeImages.arguments[i]].src = eval(changeImages.arguments[i+1] + ".src");
        }
    }
}
