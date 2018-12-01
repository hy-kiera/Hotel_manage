from django.urls import path
from . import views
from login import views as login_views

app_name = 'staff'
urlpatterns = [
    path('', views.staff_home, name='staff_home'),
    path('login', login_views.log_out, name="logout"),
]