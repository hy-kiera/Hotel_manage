from django.urls import path
from . import views
from login import views as login_views

app_name = 'staff'
urlpatterns = [
    path('', views.staff_home, name='staff_home'),
    path('login', login_views.log_out, name="logout"),
    path('guest_req', views.guest_req, name='guest_req'),
    path('guest_req/new', views.post_new, name = 'post_new'),
    path('guest_req/<pk>', views.post_detail, name='post_detail'),

]