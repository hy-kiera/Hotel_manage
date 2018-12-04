from django import forms
from staff.models import Request_post

class PostForm(forms.ModelForm):
    class Meta:
        model = Request_post
        fields = ('room_num', 'title', 'text')
