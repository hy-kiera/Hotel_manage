from django.urls import path
from . import views

urlpatterns = [
    path('', views.guest_home, name='guest_home'),
    path('myinfo', views.guest_myinfo, name = 'guest_myinfo'),
    path('payment', views.guest_payment, name='payment'),
    path('room', views.guest_room, name='guest_room'),
]
