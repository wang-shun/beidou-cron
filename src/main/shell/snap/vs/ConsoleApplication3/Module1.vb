Module Module1
    Private Declare Sub Sleep Lib "kernel32" (ByVal dwMilliseconds As Long)
    '    Dim a As IWshRuntimeLibrary.WshShell
    '    Dim WithEvents Browser As System.Windows.Forms.WebBrowser

    Dim BrowserObj As Object
    Private Declare Auto Function FindWindow Lib "user32" ( _
         ByVal lpClassName As String, _
         ByVal lpWindowName As String) As Integer
    Private Declare Function FindWindowEx Lib "user32" Alias "FindWindowExA" (ByVal hWnd1 As Long, ByVal hWnd2 As Long, ByVal lpsz1 As String, ByVal lpsz2 As String) As Long
    Private Declare Function SendMessage Lib "user32" Alias "SendMessageA" (ByVal hwnd As Long, ByVal wMsg As Long, ByVal wParam As Long, ByVal lParam As String) As Long
    Declare Function SetForegroundWindow Lib "user32" Alias "SetForegroundWindow" (ByVal hwnd As Long) As Long
    Private Declare Function ShowWindow Lib "user32" (ByVal hWnd As Long, ByVal nCmdShow As Long) As Long
    Private Declare Function IsIconic Lib "user32" (ByVal hWnd As Long) As Long

    Private Const WM_KEYDOWN = &H100
    Private Const WM_KEYUP = &H101
    Private Const WM_CHAR = &H102
    Private Const WM_SYSKEYDOWN = &H104
    Private Const WM_SYSKEYUP = &H105
    Private Const VK_SHIFT = &H10
    Private Const VK_CONTROL = &H11

    Const WM_SETHOTKEY = &H32
    Const WM_SHOWWINDOW = &H18
    Const HK_SHIFTA = &H141   'Shift   +   A   
    Const HK_SHIFTB = &H142   'Shift   +   B   
    Const HK_CONTROLA = &H241   'Control   +   A   
    Const HK_ALTZ = &H45A
    Private Const SW_RESTORE = 9

    Private Const HOTKEYF_ALT = &H4

    Sub Main()
        '        BrowserObj = CreateObject("InternetExplorer.Application")
        '        BrowserObj.Visible = True
        '        BrowserObj.Navigate("http://bbs.55bbs.com/thread-2533693-1-1.html")
        '      Sleep(3000)
        '        BrowserObj.ExecWB(4, 1, "e:\\abc.html")
        '     Sleep(2000)
        '    a.SendKeys("aa.htm")

        'Sleep(6000)

        'BrowserObj.Visible = True
        Dim params = My.Application.CommandLineArgs()

        Dim url = params.Item(0)
        Dim file = params.Item(1)

        'Dim url = "http://www.abbs.com.cn/bbs/post/view?bid=41&id=335796398&sty=1&tpg=1&ppg=97&age=0"
        'Dim file = "E:\beidou\screen\hy-toolkit\bd-screen\url2jpeg\images\test12.png"

        Dim sp = CreateObject("Wscript.Shell")

        Dim appid = sp.Run(Chr(34) + "d:\firefox\firefox.exe" + Chr(34) + " " + url + "", 4)
        'Sleep(500)

        Sleep(1000)

        Dim hWnd As Integer
        'Dim Thwnd As Long
        hWnd = FindWindow("Notepad", vbNullString)
        If hWnd > 0 Then

            '    If IsIconic(hWnd) Then
            'Call ShowWindow(hWnd, 4)
            'End If
            Call SetForegroundWindow(hWnd)
            'Thwnd = FindWindowEx(hWnd, 0, "Edit", vbNullString) '得到记事本句柄(就是我们写字的那里)

            'SendMessage(hWnd, WM_KEYDOWN, VK_CONTROL, 0)
            'SendMessage(hWnd, WM_KEYDOWN, VK_SHIFT, 0)
            'SendMessage(hWnd, WM_KEYDOWN, Asc("s"), 0)
            'SendMessage(hWnd, WM_KEYDOWN, VK_CONTROL, 1)
            'SendMessage(hWnd, WM_KEYDOWN, VK_SHIFT, 1)
            'SendMessage(hWnd, WM_KEYDOWN, Asc("s"), 1)

            'Sleep(15000)

            'Dim wHotkey As Integer

            'wHotkey = (HOTKEYF_ALT) * &H100 + Asc("Z")

            'SendMessage(hWnd, WM_SETHOTKEY, wHotkey, 0)
            ' Dim erg& = SendMessage(hWnd, WM_SETHOTKEY, HK_ALTZ, 0)

            'sp.SendKeys(url + "{ENTER}")

            ' sp.AppActivate("snap_window_beidou")

            Sleep(15000)
            '  Sleep(15000)
            'Dim hw&

            'hw& = FindWindow("MozillaUIWindowClass", vbNullString)

            'Print(hw)

            'sp.appactivate(appid)
            sp.SendKeys("^%z")

            Sleep(6000)

            'sp.appactivate(appid)
            sp.SendKeys(file)

            Sleep(1000)

            'sp.appactivate(appid)
            sp.SendKeys("{ENTER}{ENTER}")

            Sleep(5000)

            'sp.appactivate(appid)
            sp.SendKeys("^w")

            Sleep(500)
        End If


        sp.SendKeys("^w")

        Sleep(500)

        sp.SendKeys("taskkill /im firefox")

        Sleep(100)

        '       Sleep(1000)
        'sp.Run("tskill firefox")

        '       Sleep(500)
    End Sub

End Module

