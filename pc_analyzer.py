import tkinter as tk
from tkinter import ttk, filedialog, messagebox
from datetime import datetime
from collections import Counter
import csv
import re
import html
import os
import json
import shutil
from PIL import Image, ImageTk

CARDS = ['A','2','3','4','5','6','7','8','9','10','J','Q','K']
ODD = {'A','3','5','7','9','J','K'}
RESULTS = ['D','T','TIE']
RESULT_COLORS = {'D':'#16a34a','T':'#ea580c','TIE':'#7c3aed'}


def clean_card(v):
    v = str(v or '').strip().upper()
    v = re.sub(r'[^A-Z0-9]', '', v)
    if v in CARDS:
        return v
    return ''


def normalize_result(v):
    v = str(v or '').strip().upper()
    if v in ('DRAGON','D'):
        return 'D'
    if v in ('TIGER','T'):
        return 'T'
    if v in ('TIE','DRAW','SAME'):
        return 'TIE'
    return ''


class App:
    def __init__(self, root):
        self.root = root
        root.title('Dragon Tiger Analyzer - PC')
        root.geometry('1280x720')
        root.minsize(1100, 650)
        root.configure(bg='#111827')
        self.data = []
        self.dr = tk.StringVar(value='A')
        self.ti = tk.StringVar(value='A')
        self.re = tk.StringVar(value='D')
        self.pair = tk.StringVar(value='JQ')
        self.summary = tk.StringVar(value='PAIR JQ | NO HISTORY')
        self.collect_date = tk.StringVar(value=datetime.now().strftime('%d/%m/%Y'))
        self.entries = tk.IntVar(value=50)
        self.status = tk.StringVar(value='Ready')
        self.settings_dir = os.path.join(os.environ.get('APPDATA', os.path.expanduser('~')), 'DragonTigerAnalyzer')
        self.settings_file = os.path.join(self.settings_dir, 'settings.json')
        self.theme_name = 'Midnight Blue'
        self.background_path = ''
        self.bg_photo = None
        self.load_settings()
        self.build_styles()
        self.build_ui()
        self.setup_background()
        self.refresh()

    def load_settings(self):
        try:
            with open(self.settings_file, 'r', encoding='utf-8') as f:
                s = json.load(f)
            self.theme_name = s.get('theme', 'Midnight Blue')
            if self.theme_name == 'Dark Red':
                self.theme_name = 'Midnight Blue'
            self.background_path = s.get('background', '')
        except Exception:
            pass

    def save_settings(self):
        try:
            os.makedirs(self.settings_dir, exist_ok=True)
            with open(self.settings_file, 'w', encoding='utf-8') as f:
                json.dump({'theme': self.theme_name, 'background': self.background_path}, f, indent=2)
        except Exception:
            pass

    def setup_background(self):
        self.bg_canvas = tk.Canvas(self.root, highlightthickness=0, bd=0)
        self.bg_canvas.place(x=0, y=0, relwidth=1, relheight=1)
        # Canvas.lower() is overridden by Tkinter Canvas item API and requires a tag/id.
        # Lower the widget itself using the Tcl widget stacking command.
        self.root.tk.call('lower', self.bg_canvas._w)
        self.root.bind('<Configure>', self._resize_background)
        self.apply_theme(self.theme_name, save=False)

    def _resize_background(self, event=None):
        if not getattr(self, 'background_path', '') or not os.path.exists(self.background_path):
            return
        try:
            im = Image.open(self.background_path).convert('RGB')
            w, h = max(1, self.root.winfo_width()), max(1, self.root.winfo_height())
            im.thumbnail((w, h), Image.LANCZOS)
            self.bg_photo = ImageTk.PhotoImage(im)
            self.bg_canvas.delete('all')
            self.bg_canvas.create_image(w//2, h//2, image=self.bg_photo, anchor='center')
        except Exception:
            pass

    def choose_background(self):
        path = filedialog.askopenfilename(filetypes=[('Images','*.png;*.jpg;*.jpeg;*.gif;*.bmp'),('All files','*.*')])
        if not path:
            return
        try:
            os.makedirs(self.settings_dir, exist_ok=True)
            dest = os.path.join(self.settings_dir, 'background' + os.path.splitext(path)[1].lower())
            shutil.copy2(path, dest)
            self.background_path = dest
            self.save_settings()
            self._resize_background()
            self.status.set('Custom background applied and saved.')
        except Exception as e:
            messagebox.showerror('Background', str(e))

    def reset_background(self):
        self.background_path = ''
        self.save_settings()
        self.bg_photo = None
        self.bg_canvas.delete('all')
        self.bg_canvas.configure(bg=self._theme()['root'])
        self.status.set('Background reset to theme default.')

    def export_theme(self):
        path = filedialog.asksaveasfilename(defaultextension='.json', filetypes=[('Theme Settings','*.json')], initialfile='DragonTigerTheme.json')
        if not path:
            return
        try:
            payload = {'theme': self.theme_name}
            if self.background_path and os.path.exists(self.background_path):
                ext = os.path.splitext(self.background_path)[1].lower() or '.png'
                bg_dest = os.path.splitext(path)[0] + ext
                shutil.copy2(self.background_path, bg_dest)
                payload['background_file'] = os.path.basename(bg_dest)
            with open(path, 'w', encoding='utf-8') as f:
                json.dump(payload, f, indent=2)
            messagebox.showinfo('Theme Export', 'Theme settings exported successfully. If a custom background is used, its image was exported beside the JSON file.')
        except Exception as e:
            messagebox.showerror('Theme Export', str(e))

    def _theme(self):
        themes = {
            'Dark Red': {'root':'#111827','panel':'#111827','header':'#7f1d1d','heading':'#7f1d1d','button':'#374151','text':'#f9fafb','accent':'#fbbf24'},
            'Midnight Blue': {'root':'#0b1220','panel':'#0b1220','header':'#1e3a8a','heading':'#1e40af','button':'#1e293b','text':'#f8fafc','accent':'#60a5fa'},
            'Emerald': {'root':'#071a14','panel':'#071a14','header':'#065f46','heading':'#047857','button':'#1f2937','text':'#ecfdf5','accent':'#34d399'},
            'Purple': {'root':'#140d20','panel':'#140d20','header':'#581c87','heading':'#6b21a8','button':'#312e3f','text':'#faf5ff','accent':'#d8b4fe'},
            'Light': {'root':'#eef2f7','panel':'#eef2f7','header':'#334155','heading':'#475569','button':'#dbe2ea','text':'#111827','accent':'#b45309'}
        }
        return themes.get(self.theme_name, themes['Dark Red'])

    def apply_theme(self, name=None, save=True):
        if name:
            self.theme_name = name
        t = self._theme()
        def walk(w):
            try:
                cls = w.winfo_class()
                if cls in ('Frame','Labelframe','TFrame','TLabelframe'):
                    w.configure(bg=t['panel'])
                elif cls in ('Label','TLabel'):
                    w.configure(bg=t['panel'], fg=t['text'])
                elif cls in ('Button','TButton'):
                    w.configure(bg=t['button'], fg=t['text'])
                elif cls == 'Canvas' and w is not getattr(self, 'bg_canvas', None):
                    w.configure(bg=t['panel'])
            except Exception:
                pass
            for child in w.winfo_children():
                walk(child)
        walk(self.root)
        try:
            self.root.configure(bg=t['root'])
            self.bg_canvas.configure(bg=t['root'])
            self.root.children.get('!frame')
            s=ttk.Style()
            s.configure('TFrame', background=t['panel'])
            s.configure('TLabelframe', background=t['panel'], foreground=t['text'])
            s.configure('TLabelframe.Label', background=t['panel'], foreground=t['accent'])
            s.configure('TLabel', background=t['panel'], foreground=t['text'])
            s.configure('TButton', background=t['button'], foreground=t['text'])
            s.map('TButton', background=[('active',t['heading'])])
            s.configure('Treeview', background='#1f2937' if self.theme_name != 'Light' else '#ffffff', fieldbackground='#1f2937' if self.theme_name != 'Light' else '#ffffff', foreground='#f9fafb' if self.theme_name != 'Light' else '#111827')
            s.configure('Treeview.Heading', background=t['heading'], foreground='white')
            s.configure('TCombobox', fieldbackground='#1f2937' if self.theme_name != 'Light' else '#ffffff', background=t['button'], foreground=t['text'], selectbackground=t['heading'], selectforeground='white')
        except Exception:
            pass
        if save:
            self.save_settings()
        if getattr(self, 'background_path', ''):
            self._resize_background()

    def open_theme_settings(self):
        win=tk.Toplevel(self.root)
        win.title('Theme & Background')
        win.geometry('430x260')
        win.resizable(False, False)
        t=self._theme()
        win.configure(bg=t['root'])
        tk.Label(win,text='THEME & BACKGROUND',bg=t['root'],fg=t['accent'],font=('Segoe UI',14,'bold')).pack(pady=(18,12))
        row=tk.Frame(win,bg=t['root']); row.pack(fill='x',padx=25,pady=6)
        tk.Label(row,text='Theme',bg=t['root'],fg=t['text'],font=('Segoe UI',10,'bold')).pack(side='left')
        theme_var=tk.StringVar(value=self.theme_name)
        cb=ttk.Combobox(row,textvariable=theme_var,values=['Dark Red','Midnight Blue','Emerald','Purple','Light'],state='readonly',width=20)
        cb.pack(side='right')
        def apply_and_close():
            self.apply_theme(theme_var.get())
            win.destroy()
        tk.Button(win,text='APPLY THEME',command=apply_and_close,bg=t['heading'],fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=16,pady=7).pack(pady=8)
        tk.Button(win,text='CHOOSE BACKGROUND IMAGE',command=self.choose_background,bg=t['button'],fg=t['text'],font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).pack(pady=4)
        tk.Button(win,text='RESET BACKGROUND',command=self.reset_background,bg=t['button'],fg=t['text'],font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).pack(pady=4)
        tk.Button(win,text='EXPORT THEME',command=self.export_theme,bg=t['button'],fg=t['text'],font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).pack(pady=4)

    def build_styles(self):
        s = ttk.Style()
        s.theme_use('clam')
        s.configure('TFrame', background='#111827')
        s.configure('TLabelframe', background='#111827', foreground='#e5e7eb')
        s.configure('TLabelframe.Label', background='#111827', foreground='#fbbf24', font=('Segoe UI',11,'bold'))
        s.configure('TLabel', background='#111827', foreground='#f3f4f6', font=('Segoe UI',10))
        s.configure('TButton', font=('Segoe UI',10,'bold'), padding=(11,7), background='#374151', foreground='white')
        s.map('TButton', background=[('active','#4b5563')])
        s.configure('TCombobox', fieldbackground='#1f2937', background='#374151', foreground='#f9fafb', selectbackground='#2563eb', selectforeground='white')
        s.map('TCombobox', fieldbackground=[('readonly','#1f2937')], foreground=[('readonly','#f9fafb')], selectbackground=[('readonly','#2563eb')], selectforeground=[('readonly','white')])
        s.configure('Treeview', background='#1f2937', fieldbackground='#1f2937', foreground='#f9fafb', rowheight=32, font=('Segoe UI',9))
        s.configure('Treeview.Heading', background='#7f1d1d', foreground='white', font=('Segoe UI',9,'bold'), padding=7)

    def build_ui(self):
        header = tk.Frame(self.root, bg='#7f1d1d', height=68)
        header.pack(fill='x')
        for text, bg, width in [('DRAGON','#991b1b',14),('TIE','#312e81',9),('TIGER','#111827',14),('PAIR','#991b1b',14)]:
            tk.Label(header,text=text,bg=bg,fg='white',font=('Segoe UI',16,'bold'),width=width,pady=10).pack(side='left',padx=4,pady=9)
        tk.Label(header,text='PC ANALYZER',bg='#7f1d1d',fg='#fde68a',font=('Segoe UI',15,'bold')).pack(side='right',padx=20)

        tools = tk.Frame(self.root,bg='#111827'); tools.pack(fill='x',padx=18,pady=(11,4))
        for text,cmd in [('Import Excel/CSV',self.import_data),('Export Excel/CSV',self.export_data),('THEME / BACKGROUND',self.open_theme_settings)]:
            tk.Button(tools,text=text,command=cmd,bg='#374151',fg='white',activebackground='#4b5563',activeforeground='white',font=('Segoe UI',10,'bold'),relief='flat',padx=11,pady=7).pack(side='left',padx=4)
        tk.Label(tools,text='Historical statistical reference only',bg='#111827',fg='#9ca3af',font=('Segoe UI',9)).pack(side='right',padx=10)

        pair = tk.LabelFrame(self.root,text='  PAIR REFERENCE / STATISTICAL PREDICTION  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        pair.pack(fill='x',padx=18,pady=6)
        tk.Label(pair,text='Pair',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).pack(side='left',padx=(12,5),pady=10)
        ttk.Combobox(pair,textvariable=self.pair,values=[a+b for a in CARDS for b in CARDS],width=12).pack(side='left',padx=5)
        tk.Button(pair,text='ANALYZE',command=self.analyze,bg='#b91c1c',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=14,pady=6).pack(side='left',padx=6)
        tk.Label(pair,textvariable=self.summary,bg='#111827',fg='#fde68a',font=('Segoe UI',10,'bold')).pack(side='left',padx=20)
        chart_box=tk.LabelFrame(self.root,text='  NUMBER / DRAGON-TIGER FREQUENCY CHART  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        chart_box.pack(fill='x',padx=18,pady=6)
        self.chart_frame=tk.Frame(chart_box,bg='#111827',height=150); self.chart_frame.pack(fill='x',padx=10,pady=8)
        # Occurrence report is kept; Round Analysis Graph is removed.
        report = tk.LabelFrame(self.root,text='  OCCURRENCE + PREVIOUS/NEXT 3-ROUND REPORT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        report.pack(fill='x',padx=18,pady=6)
        report_table = tk.Frame(report,bg='#111827'); report_table.pack(fill='x',padx=8,pady=6)
        rcols=('OCCURRENCE','PREVIOUS 3','MATCH','NEXT 1','NEXT 2','NEXT 3','NEXT 4','NEXT 5','NEXT 6')
        self.report_tree=ttk.Treeview(report_table,columns=rcols,show='headings',height=7)
        rwidths={'OCCURRENCE':90,'PREVIOUS 3':250,'MATCH':90,'NEXT 1':90,'NEXT 2':90,'NEXT 3':90,'NEXT 4':90,'NEXT 5':90,'NEXT 6':90}
        for c in rcols:
            self.report_tree.heading(c,text=c); self.report_tree.column(c,width=rwidths[c],anchor='center')
        self.report_tree.pack(side='left',fill='both',expand=True)
        ry=ttk.Scrollbar(report_table,orient='vertical',command=self.report_tree.yview)
        ry.pack(side='right',fill='y')
        self.report_tree.configure(yscrollcommand=ry.set)
        report_table = tk.Frame(report,bg='#111827'); report_table.pack(fill='x',padx=8,pady=6)
        rcols=('OCCURRENCE','PREVIOUS 3','MATCH','NEXT 1','NEXT 2','NEXT 3','NEXT 4','NEXT 5','NEXT 6')
        self.report_tree=ttk.Treeview(report_table,columns=rcols,show='headings',height=6)
        rwidths={'OCCURRENCE':90,'PREVIOUS 3':250,'MATCH':90,'NEXT 1':90,'NEXT 2':90,'NEXT 3':90,'NEXT 4':90,'NEXT 5':90,'NEXT 6':90}
        for c in rcols:
            self.report_tree.heading(c,text=c); self.report_tree.column(c,width=rwidths[c],anchor='center')
        self.report_tree.pack(fill='x',expand=True)

        table = tk.Frame(self.root,bg='#111827'); table.pack(fill='both',expand=True,padx=18,pady=(4,8))
        cols=('S NO','ROUND ID','TIME','DRAGON','TIGER','RESULT','PAIR','D O/E','T O/E','PREV RESULT')
        self.tree=ttk.Treeview(table,columns=cols,show='headings')
        widths={'S NO':60,'ROUND ID':155,'TIME':135,'DRAGON':75,'TIGER':75,'RESULT':75,'PAIR':75,'D O/E':85,'T O/E':85,'PREV RESULT':105}
        for c in cols:
            self.tree.heading(c,text=c); self.tree.column(c,width=widths[c],anchor='center')
        # Unique result colors: D=green, T=orange, TIE=purple.
        for tag,color in [('dragon','#16a34a'),('tiger','#ea580c'),('tie','#7c3aed')]:
            self.tree.tag_configure(tag,background=color,foreground='white')
        self.report_tree.tag_configure('dragon',background='#14532d',foreground='white')
        self.report_tree.tag_configure('tiger',background='#92400e',foreground='white')
        self.report_tree.tag_configure('tie',background='#4c1d95',foreground='white')
        self.tree.pack(side='left',fill='both',expand=True)
        y=ttk.Scrollbar(table,orient='vertical',command=self.tree.yview); y.pack(side='right',fill='y'); self.tree.configure(yscrollcommand=y.set)

        # LAST RESULT stays fixed at the very bottom.
        last = tk.LabelFrame(self.root,text='  LAST RESULT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        last.pack(fill='x',padx=18,pady=(0,8))
        self.last_frame = tk.Frame(last,bg='#111827',height=82)
        self.last_frame.pack(fill='x',padx=10,pady=6)
        self.last_frame.pack_propagate(False)
        self.last_analysis = tk.StringVar(value='LAST RESULT ANALYSIS | Import rounds to see previous history, card numbers and next historical rounds')
        tk.Label(last,textvariable=self.last_analysis,bg='#111827',fg='#fde68a',font=('Segoe UI',9,'bold'),anchor='w').pack(fill='x',padx=10,pady=(0,5))

    def oe(self,c): return 'ODD' if c in ODD else 'EVEN'
    def tag_for(self,r): return {'D':'dragon','T':'tiger','TIE':'tie'}[r]

    def add_result(self):
        n=max([int(r['sno']) for r in self.data if str(r.get('sno','')).isdigit()] or [0])+1
        self.data.append({'sno':n,'round_id':'','time':'','dragon':self.dr.get(),'tiger':self.ti.get(),'result':self.re.get(),'date':''})
        self.refresh()

    def clear_all(self):
        if messagebox.askyesno('Confirm','Delete all loaded rounds?'):
            self.data=[]; self.refresh()

    def refresh(self, rows=None):
        rows = self.data if rows is None else rows
        for x in self.tree.get_children(): self.tree.delete(x)
        for i,r in enumerate(rows):
            prev = rows[i-1].get('result','') if i else ''
            self.tree.insert('', 'end', values=(r.get('sno',''),r.get('round_id',''),r.get('time',''),r.get('dragon',''),r.get('tiger',''),r.get('result',''),r.get('dragon','')+r.get('tiger',''),self.oe(r.get('dragon','')),self.oe(r.get('tiger','')),prev),tags=(self.tag_for(r.get('result','D')),))
        for x in self.last_frame.winfo_children(): x.destroy()
        recent = self.data[-14:]
        for r in recent:
            res=r.get('result','')
            card=f'{r.get("dragon","")}/{r.get("tiger","")}'
            color=RESULT_COLORS.get(res,'#374151')
            box=tk.Canvas(self.last_frame,width=58,height=78,bg='#111827',highlightthickness=0)
            box.create_oval(5,3,53,51,fill=color,outline='')
            box.create_text(29,27,text=res,fill='white',font=('Segoe UI',11,'bold'))
            box.create_text(29,64,text=card,fill='white',font=('Segoe UI',8,'bold'))
            box.pack(side='right',padx=2)
        self.update_last_analysis()
        self.update_title_counts()

    def update_last_analysis(self):
        if not self.data:
            self.last_analysis.set('LAST RESULT ANALYSIS | No rounds loaded')
            return
        idx=len(self.data)-1
        cur=self.data[idx]
        cur_pair=f'{cur.get("dragon","")}{cur.get("tiger","")}'
        cur_desc=f'{cur.get("result","")} | D:{cur.get("dragon","")} ({self.oe(cur.get("dragon",""))}) | T:{cur.get("tiger","")} ({self.oe(cur.get("tiger",""))}) | Pair:{cur_pair}'
        prev=[]
        for j in range(max(0,idx-5),idx):
            q=self.data[j]
            prev.append(f'{q.get("result","")}[{q.get("dragon","")}/{q.get("tiger","")}]')
        # Historical occurrences of the current result + pair: show what came immediately after each occurrence.
        matches=[]
        for j,r in enumerate(self.data[:-1]):
            if r.get("result")==cur.get("result") and r.get("dragon","")+r.get("tiger","")==cur_pair:
                nxt=self.data[j+1]
                matches.append(f'{nxt.get("result","")} D:{nxt.get("dragon","")} T:{nxt.get("tiger","")}')
        after=' | '.join(matches[-3:]) if matches else 'No previous matching occurrence with a following round'
        self.last_analysis.set(
            f'LAST: {cur_desc}   |   PREVIOUS 5: {"  ".join(prev) if prev else "-"}   |   AFTER SAME RESULT+PAIR: {after}'
        )

    def update_title_counts(self):
        c=Counter(r.get('result') for r in self.data)
        self.summary.set(f'PAIR {self.pair.get().upper()} | D {c["D"]} | T {c["T"]} | TIE {c["TIE"]} | TOTAL {len(self.data)}')

    def analyze(self):
        p=self.pair.get().upper().strip()
        rows=[r for r in self.data if r.get('dragon','')+r.get('tiger','')==p]
        cnt=Counter(r.get('result') for r in rows)
        for x in self.report_tree.get_children(): self.report_tree.delete(x)
        if not rows:
            self.summary.set(f'PAIR {p} | NO HISTORY')

            return
        maxres=max(RESULTS,key=lambda x:cnt[x]); pct=cnt[maxres]/len(rows)*100
        self.summary.set(f'PAIR {p} | CAME {len(rows)} TIMES | D {cnt["D"]} | T {cnt["T"]} | TIE {cnt["TIE"]} | MOST {maxres} ({pct:.1f}%)')
        self.draw_frequency_chart(rows)

        for idx,r in enumerate(self.data):
            if r.get('dragon','')+r.get('tiger','') != p: continue
            prev=[]
            for j in range(max(0,idx-3),idx):
                q=self.data[j]; prev.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            nxt=[]
            for j in range(idx+1,min(len(self.data),idx+7)):
                q=self.data[j]; nxt.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            while len(nxt)<6: nxt.append('-')
            self.report_tree.insert('', 'end', values=(r.get('sno',''),' | '.join(prev) if prev else '-',f'{r.get("result","")} {p}',nxt[0],nxt[1],nxt[2],nxt[3],nxt[4],nxt[5]),tags=(self.tag_for(r.get('result','D')),))

    def draw_frequency_chart(self, rows):
        for w in self.chart_frame.winfo_children(): w.destroy()
        if not rows:
            tk.Label(self.chart_frame,text='No matching pair history',bg='#111827',fg='#9ca3af').pack(pady=25)
            return
        counts=Counter()
        for r in rows:
            d,t=r.get('dragon',''),r.get('tiger','')
            if d: counts[(d,'D')]+=1
            if t: counts[(t,'T')]+=1
        top=sorted(counts.items(),key=lambda x:(-x[1],x[0][0],x[0][1]))[:13]
        maxv=max(v for _,v in top) or 1
        for (num,side),v in top:
            row=tk.Frame(self.chart_frame,bg='#111827'); row.pack(fill='x',pady=2)
            tk.Label(row,text=f'{num}  {side}',bg='#14532d' if side=='D' else '#92400e',fg='white',width=7,font=('Segoe UI',9,'bold')).pack(side='left')
            bar=tk.Canvas(row,height=20,bg='#1f2937',highlightthickness=0); bar.pack(side='left',fill='x',expand=True,padx=6)
            bar.update_idletasks()
            w=max(30,int(bar.winfo_width()*v/maxv))
            bar.create_rectangle(0,2,w,18,fill='#22c55e' if side=='D' else '#f59e0b',outline='')
            tk.Label(row,text=f'{v} times',bg='#111827',fg='white',width=10,anchor='e',font=('Segoe UI',9,'bold')).pack(side='right')

    def draw_round_graph(self):
        cv=self.round_chart
        cv.delete('all')
        data=self.data[-40:]
        if not data:
            cv.create_text(20,80,anchor='w',text='Import data to view round analysis',fill='#9ca3af',font=('Segoe UI',10))
            return
        cv.update_idletasks()
        W=max(cv.winfo_width(),700); H=180
        left=45; right=20; top=18; bottom=38
        usable=max(1,W-left-right)
        step=usable/max(1,len(data))
        barw=max(6,min(18,step*0.65))
        cv.create_line(left,H-bottom,left+usable,H-bottom,fill='#4b5563')
        for i,r in enumerate(data):
            x=left+i*step+step/2
            res=r.get('result','')
            h={'D':85,'T':65,'TIE':45}.get(res,35)
            color={'D':'#22c55e','T':'#f59e0b','TIE':'#a855f7'}.get(res,'#6b7280')
            cv.create_rectangle(x-barw/2,H-bottom-h,x+barw/2,H-bottom,fill=color,outline='')
            if len(data)<=20 or i%2==0:
                cv.create_text(x,H-bottom-h-9,text=res,fill='white',font=('Segoe UI',8,'bold'))
            label=str(r.get('sno',''))
            if len(data)<=25 or i%4==0:
                cv.create_text(x,H-bottom+12,text=label,fill='#9ca3af',font=('Segoe UI',8))
            num=f"{r.get('dragon','')}/{r.get('tiger','')}"
            if len(data)<=20 or i%3==0:
                cv.create_text(x,H-bottom+27,text=num,fill='#d1d5db',font=('Segoe UI',7))
        cv.create_text(left,8,anchor='w',text='Latest 40 rounds | bar = result | label = D/T card numbers',fill='#cbd5e1',font=('Segoe UI',9,'bold'))

    def import_data(self):
        path=filedialog.askopenfilename(filetypes=[('Excel','*.xlsx'),('CSV','*.csv'),('All files','*.*')])
        if not path:return
        try:
            rows=[]
            if path.lower().endswith('.csv'):
                with open(path,newline='',encoding='utf-8-sig') as f: rows=list(csv.reader(f))
            else:
                from openpyxl import load_workbook
                wb=load_workbook(path,data_only=True); ws=wb.active; rows=list(ws.iter_rows(values_only=True))
            out=[]; seen=set()
            for row in rows:
                if len(row)<4: continue
                try: sno=int(row[0])
                except: continue
                d=clean_card(row[1]); t=clean_card(row[2]); res=normalize_result(row[3])
                if d and t and res and sno not in seen:
                    out.append({'sno':sno,'round_id':str(row[4]) if len(row)>4 else '','time':str(row[5]) if len(row)>5 else '','dragon':d,'tiger':t,'result':res,'date':str(row[6]) if len(row)>6 else ''}); seen.add(sno)
            out.sort(key=lambda r:r['sno']); self.data=out; self.refresh(); messagebox.showinfo('Import',f'Imported {len(out)} unique rounds.')
        except Exception as e: messagebox.showerror('Import error',str(e))

    def export_data(self):
        if not self.data: messagebox.showwarning('Export','No data to export.'); return
        path=filedialog.asksaveasfilename(defaultextension='.xlsx',filetypes=[('Excel','*.xlsx'),('CSV','*.csv')])
        if not path:return
        try:
            rows=[['S NO','ROUND ID','TIME','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','DATE']]
            for r in self.data: rows.append([r.get('sno',''),r.get('round_id',''),r.get('time',''),r.get('dragon',''),r.get('tiger',''),r.get('result',''),r.get('dragon','')+r.get('tiger',''),self.oe(r.get('dragon','')),self.oe(r.get('tiger','')),r.get('date','')])
            if path.lower().endswith('.csv'):
                with open(path,'w',newline='',encoding='utf-8-sig') as f: csv.writer(f).writerows(rows)
            else:
                from openpyxl import Workbook
                wb=Workbook(); ws=wb.active; ws.title='ANALYZER'
                for row in rows: ws.append(row)
                wb.save(path)
            messagebox.showinfo('Export','Export completed successfully.')
        except Exception as e: messagebox.showerror('Export error',str(e))

    def parse_pasted_rows(self, text):
        lines=[x.strip() for x in text.replace('\r','').split('\n') if x.strip()]
        out=[]
        for line in lines:
            parts=re.split(r'\t|\s{2,}|\|',line)
            joined=' '.join(parts)
            if re.search(r'round\s*id|match\s*time|winner',joined,re.I):
                continue
            rid=''
            m=re.search(r'\b(\d{10,20})\b',line)
            if m: rid=m.group(1)
            res=normalize_result(re.search(r'\b(DRAGON|TIGER|TIE|DRAW)\b',line,re.I).group(1)) if re.search(r'\b(DRAGON|TIGER|TIE|DRAW)\b',line,re.I) else ''
            cards=[]
            for token in re.findall(r'(?<![A-Z0-9])(10|[2-9AJQK])(?![A-Z0-9])',line.upper()): cards.append(token)
            if rid and res:
                d=cards[0] if len(cards)>=2 else ''
                t=cards[1] if len(cards)>=2 else ''
                out.append({'round_id':rid,'time':'','dragon':d,'tiger':t,'result':res,'date':self.collect_date.get()})
        return out

    def merge_collected(self, incoming):
        if not incoming: return 0
        existing_ids={str(r.get('round_id','')) for r in self.data if r.get('round_id')}
        next_sno=max([int(r['sno']) for r in self.data if str(r.get('sno','')).isdigit()] or [0])+1
        added=0
        limit=max(1,int(self.entries.get()))
        for r in incoming[:limit]:
            rid=str(r.get('round_id','')).strip()
            if not rid or rid in existing_ids: continue
            r['sno']=next_sno; next_sno+=1; self.data.append(r); existing_ids.add(rid); added+=1
        self.data.sort(key=lambda x:int(x.get('sno',0)) if str(x.get('sno','')).isdigit() else 0)
        self.refresh()
        return added

    def paste_results(self):
        try:
            text=self.root.clipboard_get()
        except Exception:
            messagebox.showwarning('Paste Results','Copy the YoloBet9 Casino Results rows first, then click PASTE RESULTS.')
            return
        incoming=self.parse_pasted_rows(text)
        if not incoming:
            messagebox.showwarning('Paste Results','No Round ID + Winner rows were detected. Copy the result table rows and try again.')
            return
        added=self.merge_collected(incoming)
        self.status.set(f'Date {self.collect_date.get()} | detected {len(incoming)} | added {added} | duplicates skipped')
        messagebox.showinfo('YoloBet9 Import',f'Detected {len(incoming)} results. Added {added} new unique Round IDs. Duplicate Round IDs were skipped.')

    def filter_date(self):
        target=self.collect_date.get().strip()
        rows=[]
        for r in self.data:
            d=str(r.get('date','')).strip()
            if d==target: rows.append(r)
            elif not d and str(r.get('time','')).startswith(target): rows.append(r)
        self.refresh(rows)
        self.status.set(f'Date filter {target} | showing {len(rows)} stored rounds')


if __name__=='__main__':
    root=tk.Tk(); App(root); root.mainloop()
