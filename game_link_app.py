import os
import subprocess
import tkinter as tk
from tkinter import messagebox
import webbrowser
import time
import threading
import ctypes

try:
    import webview
except Exception:
    webview = None

APP_TITLE = "Dragon Tiger Game Link"
DEFAULT_URL = "https://"

class GameLinkApp:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title(APP_TITLE)
        self.root.geometry("760x180")
        self.root.minsize(620, 160)
        self.root.configure(bg="#071a2b")
        tk.Label(self.root, text="GAME LINK", bg="#071a2b", fg="#00e5ff",
                 font=("Segoe UI", 16, "bold")).pack(pady=(18, 6))
        self.url = tk.StringVar(value=DEFAULT_URL)
        row = tk.Frame(self.root, bg="#071a2b")
        row.pack(fill="x", padx=20)
        self.entry = tk.Entry(row, textvariable=self.url, bg="#102b40", fg="white",
                              insertbackground="white", font=("Segoe UI", 11), relief="flat")
        self.entry.pack(side="left", fill="x", expand=True, ipady=9, padx=(0, 8))
        tk.Button(row, text="OPEN GAME", command=self.open_game, bg="#00a8ff", fg="white",
                  font=("Segoe UI", 10, "bold"), relief="flat", padx=16, pady=9).pack(side="left")
        tk.Button(row, text="GAME LEFT / DASHBOARD RIGHT", command=self.open_split,
                  bg="#8e44ad", fg="white", font=("Segoe UI", 10, "bold"),
                  relief="flat", padx=12, pady=9).pack(side="left", padx=(8, 0))

        buttons = tk.Frame(self.root, bg="#071a2b")
        buttons.pack(pady=18)
        tk.Button(buttons, text="OPEN ANALYZER APP", command=self.open_analyzer,
                  bg="#00a86b", fg="white", font=("Segoe UI", 10, "bold"),
                  relief="flat", padx=18, pady=9).pack(side="left", padx=6)
        tk.Button(buttons, text="OPEN IN BROWSER", command=self.open_browser,
                  bg="#34495e", fg="white", font=("Segoe UI", 10, "bold"),
                  relief="flat", padx=18, pady=9).pack(side="left", padx=6)
        tk.Label(self.root,
                 text="Separate app: it does not modify the Analyzer database or source.",
                 bg="#071a2b", fg="#9fb3c8", font=("Segoe UI", 9)).pack()
        self.entry.focus_set()
        self.root.bind("<Return>", lambda e: self.open_game())

    def valid_url(self):
        u = self.url.get().strip()
        if not (u.startswith("http://") or u.startswith("https://")):
            messagebox.showwarning("Game Link", "Please enter a valid http:// or https:// game link.")
            return None
        return u

    def open_browser(self):
        u = self.valid_url()
        if u:
            webbrowser.open(u)

    def find_analyzer_exe(self):
        candidates = [
            os.path.join(os.environ.get("ProgramFiles", ""), "Dragon Tiger Analyzer v2.0", "DragonTigerAnalyzerPC.exe"),
            os.path.join(os.environ.get("ProgramFiles(x86)", ""), "Dragon Tiger Analyzer v2.0", "DragonTigerAnalyzerPC.exe"),
        ]
        for path in candidates:
            if path and os.path.exists(path):
                return path
        return None

    def _find_analyzer_window(self):
        target = "Dragon Tiger Analyzer - PC v2.0"
        found = []
        user32 = ctypes.windll.user32
        EnumWindowsProc = ctypes.WINFUNCTYPE(ctypes.c_bool, ctypes.c_void_p, ctypes.c_void_p)
        def callback(hwnd, lparam):
            n = user32.GetWindowTextLengthW(hwnd)
            if n:
                buf = ctypes.create_unicode_buffer(n + 1)
                user32.GetWindowTextW(hwnd, buf, n + 1)
                if target.lower() in buf.value.lower():
                    found.append(hwnd)
                    return False
            return True
        user32.EnumWindows(EnumWindowsProc(callback), 0)
        return found[0] if found else None

    def _move_analyzer_right(self):
        if os.name != "nt":
            return
        user32 = ctypes.windll.user32
        sw = user32.GetSystemMetrics(0)
        sh = user32.GetSystemMetrics(1)
        left_w = max(500, sw // 2)
        right_w = sw - left_w
        for _ in range(80):
            hwnd = self._find_analyzer_window()
            if hwnd:
                user32.MoveWindow(hwnd, left_w, 0, right_w, sh, True)
                return
            time.sleep(0.25)

    def open_split(self):
        u = self.valid_url()
        if not u:
            return
        analyzer = self.find_analyzer_exe()
        if not analyzer:
            messagebox.showinfo("Analyzer App",
                                "Analyzer app was not found. Install the Analyzer app first.")
            return
        try:
            subprocess.Popen([analyzer])
            threading.Thread(target=self._move_analyzer_right, daemon=True).start()
            if webview is None:
                webbrowser.open(u)
                return
            sw = ctypes.windll.user32.GetSystemMetrics(0) if os.name == "nt" else 1920
            sh = ctypes.windll.user32.GetSystemMetrics(1) if os.name == "nt" else 1080
            left_w = max(500, sw // 2)
            webview.create_window("Game - Left Side", u, x=0, y=0,
                                  width=left_w, height=sh, min_size=(500, 500), resizable=True)
            webview.start()
        except Exception as e:
            messagebox.showerror("Split View", f"Unable to open split view: {e}")

    def open_game(self):
        u = self.valid_url()
        if not u:
            return
        if webview is None:
            self.open_browser()
            return
        try:
            webview.create_window("Game", u, width=1100, height=760, min_size=(700, 500))
            webview.start()
        except Exception:
            self.open_browser()

    def open_analyzer(self):
        path = self.find_analyzer_exe()
        if path:
            subprocess.Popen([path])
            return
        messagebox.showinfo("Analyzer App",
                            "Analyzer app was not found in the standard install location.\n"
                            "Start it separately from its desktop shortcut.")

if __name__ == "__main__":
    GameLinkApp().root.mainloop()
