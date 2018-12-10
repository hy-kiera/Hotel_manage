from django.contrib import admin
from django.contrib.auth.models import User
from django.contrib.auth.admin import UserAdmin
from .models import Room, Request_post, Department, Staff

admin.site.unregister(User)

class ProfileInline(admin.StackedInline):
    model = Staff
    can_delete = False
    verbose_name = 'StaffProfile'
    fk_name = 'user'

class ProfileAdmin(UserAdmin):
    inlines = (ProfileInline, )

    def get_inline_instances(self, request, obj=None):
        if not obj:
            return list()
        return super(ProfileAdmin, self).get_inline_instances(request, obj)

admin.site.register(User, ProfileAdmin)
admin.site.register(Room)
admin.site.register(Request_post) 
admin.site.register(Department)  
admin.site.register(Staff)