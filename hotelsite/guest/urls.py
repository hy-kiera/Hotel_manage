from django.urls import path
from . import views

app_name = 'guest'
urlpatterns = [
    path('', views.guest_home, name='guest_home'),
    path('myinfo/', views.guest_myinfo, name = 'guest_myinfo'),
    path('payment/', views.guest_payment, name='payment'),
    path('room/', views.guest_room, name='guest_room'),
    path('req/', views.req, name='req'),
    path('req/req_detail/<pk>', views.post_detail, name = 'post_detail'),
    path('req/req_new/', views.post_new, name='post_new'),
    path('introduce/', views.introduce, name='introduce'),
    path('myreserv/', views.myreserv, name='myreserv'),
    path('req/req_edit/<pk>', views.post_edit, name='post_edit'),
]


