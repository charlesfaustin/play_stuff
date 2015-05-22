$ ->
  $.get "/musics", (musics) ->
    $.each musics, (index, music) ->
      $("#advanced-2").append $("<li>").toggleClass(music.idstring).text music.filename.substring(0,6) + "..."   # + "  " + music.idstring 