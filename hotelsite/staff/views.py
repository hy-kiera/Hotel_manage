from django.shortcuts import render, get_object_or_404
from .models import Room, Request_post, Department
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from .forms import PostForm
from django.http import HttpResponse

@login_required(login_url='login:sign_in')
def staff_home(request):
    rooms = Room.objects.order_by('room_num')
    return render(request, 'staff/staff_home.html', {'rooms':rooms})

def guest_req(request): 
    posts = Request_post.objects.order_by('handle_or_not') 
    form = PostForm()
    return render(request, 'staff/guest_req.html', {'posts': posts,'form':form})

def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            if post.dept != None:
                post.handle_or_not= 2
            post.save()
            return redirect('staff:guest_req')
    else:
        form = PostForm()
    return render(request, 'staff/req_edit.html', {'form':form})

def post_detail(request, pk):
    post = get_object_or_404(Request_post, pk=pk)
    if request.method == "POST":
        if request.POST['dept'] != '':
            dept = Department.objects.get(id=request.POST['dept'])
            post.dept = dept
            post.handle_or_not = 2

        else:
            post.dept = None
            post.handle_or_not = 1
        post.save()
        return redirect('staff:guest_req')
    else:
        form = PostForm(instance=post)
        return render(request, 'staff/req_detail.html', {'post':post, 'form':form})
