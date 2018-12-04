from django import forms
from .models import Request_post, Department, Room

class PostForm(forms.ModelForm):
    class Meta:
        model = Request_post
        fields = ('room_num','author', 'title', 'text','dept')

