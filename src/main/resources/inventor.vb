Public parts As Dictionary
Dim dirs As Dictionary

Sub resize()

    Dim Inv As Inventor.Application
    
    Dim pot As String
    Dim assFileName As String
    Dim partFileName As String
    Dim fullAssFileName As String
    Dim fullPartFileName As String
    
    Dim oDoc As Inventor.Document
    Dim oDef As Inventor.ComponentDefinition
    Dim oFactory As iPartFactory
    

    Dim oAss As AssemblyDocument
    Dim oPart As PartDocument
    Dim oCompDef As ComponentDefinition
    Dim oFeature As FeatureApproximationTypeEnum
        
    Dim r As Range, c As Range
        
    Dim oExtrudeFeature As ExtrudeFeature
    Dim kv_cev_a As Single
    
    
    Set Inv = GetObject(, "Inventor.Application")
    'Set Inv = GetObject(, "{B6B5DC40-96E3-11d2-B774-0060B0F159EF}")
  
    'Set Inv = CreateObject("new:{B6B5DC40-96E3-11d2-B774-0060B0F159EF}")
    Set oAss = Inv.ActiveDocument
    Set oDef = oAss.ComponentDefinition
    
    Dim jeCev As Boolean
    Dim sirina As Single, globina As Single, visina As Single, kot As Single
    
    Set r = ThisWorkbook.Worksheets("Nova").Range("A2..A5")
    
    For Each c In r
        assFileName = c.value
        
        pot = c.Offset(0, 1).value
        
        fullAssFileName = pot & "\" & assFileName + ".iam"
        
        sirina = c.Offset(0, 2).value
        globina = c.Offset(0, 3).value
        visina = c.Offset(0, 4).value
        kot = c.Offset(0, 5).value
        
        kv_cev_a = c.Offset(0, 6).value
        kv_cev_stena = c.Offset(0, 7).value
        
        pr_cev_a = c.Offset(0, 8).value
        pr_cev_b = c.Offset(0, 9).value
        pr_cev_stena = c.Offset(0, 10).value
            
        oAss.ComponentDefinition.Parameters("sirina").Expression = sirina
        oAss.ComponentDefinition.Parameters("globina").Expression = globina
        oAss.ComponentDefinition.Parameters("visina").Expression = visina
        oAss.ComponentDefinition.Parameters("kot").Expression = kot
        
        For i = 1 To oAss.ReferencedDocuments.Count
        
            If Right(oAss.ReferencedDocuments.Item(i).File.FullFileName, 4) = ".ipt" Then
                Set oPart = oAss.ReferencedDocuments.Item(i)
                
                Debug.Print oPart.File.FullFileName
                'On Error Resume Next
                'oPart.Activate
                'On Error GoTo 0
                
                where = InStrRev(oPart.File.FullFileName, "\") + 1
                
                fullPartFileName = pot & "\" & Mid(oPart.File.FullFileName, where, Len(oPart.File.FullFileName) - where + 2)
                
                jevcev = False
                If oPart.DisplayName = "CevStranskaHor" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = globina
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                ElseIf oPart.DisplayName = "CevStranskaVer" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = visina
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                ElseIf oPart.DisplayName = "CevKoncnaTop" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = sirina
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                ElseIf oPart.DisplayName = "CevKoncnaBottom" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = sirina
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                ElseIf oPart.DisplayName = "CevSrednjaZgornjaLeva" Or oPart.DisplayName = "CevSrednjaZgornjaDesna" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = globina - kv_cev_a
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                ElseIf oPart.DisplayName = "CevSrednjaSpodnjaLeva" Or oPart.DisplayName = "CevSrednjaSpodnjaDesna" Then
                    oPart.ComponentDefinition.Parameters("dolzina").Expression = visina - kv_cev_a
                    oPart.ComponentDefinition.Parameters("kot").Expression = kot
                    jeCev = True
                End If
                
                If jeCev Then
                    oPart.Rebuild
                    oPart.Rebuild2 True
                    
                    On Error Resume Next
                    Debug.Print "    " & fullPartFileName
                    oPart.SaveAs fullPartFileName, False
                    On Error GoTo 0
                End If
            End If
        Next i
        
        Debug.Print fullAssFileName
        oAss.Activate
        'oAss.Rebuild
        oAss.Rebuild2 True
        
        'On Error Resume Next
        oAss.SaveAs fullAssFileName, True
        'On Error GoTo 0
    Next c
    
End Sub


Function FileExists(ByVal FileToTest As String) As Boolean
   FileExists = (dir(FileToTest) <> "")
End Function

Sub DeleteFile(ByVal FileToDelete As String)
   If FileExists(FileToDelete) Then 'See above
      SetAttr FileToDelete, vbNormal
      Kill FileToDelete
   End If
End Sub



Sub Test()

    Dim Inv As Inventor.Application
    Set Inv = GetObject(, "Inventor.Application")
    'Set Inv = CreateObject("new:{B6B5DC40-96E3-11d2-B774-0060B0F159EF}")

    Dim oSelectSet As Inventor.SelectSet
    Dim oFace As Inventor.Face

    
    Set oSelectSet = Inv.ActiveDocument.SelectSet
    
    On Error Resume Next
    If oSelectSet.Count = 0 Then
        Exit Sub
    End If
    
    ' check if it's a face
    Set oFace = oSelectSet.Item(1)
    
    If oFace Is Nothing Then
        Exit Sub
    End If
    On Error GoTo 0
    
    ' get the native object, which resides in the definition of the occurrence

    ' get the document where the face belongs
    Dim oDoc As Document
    Set oDoc = oFace.Parent.ComponentDefinition.Document
    
    ' Place the base front view.
    Dim oFrontView As DrawingView
    oFrontView = oSheet.DrawingViews.AddBaseView(oPartDoc, oTG.CreatePoint2d(15, 12), 3, ViewOrientationTypeEnum.kFrontViewOrientation, DrawingViewStyleEnum.kHiddenLineDrawingViewStyle)
    
    Dim oTG As TransientGeometry
    oTG = Inv.TransientGeometry
    
    Dim oSectionSketch As DrawingSketch
    oSectionSketch = oFrontView.Sketches.Add
    oSectionSketch.Edit
    
    
    
    
    For Each oFace In oSelectSet
        
    Next
    
    Call oFace.SetRenderStyle(kOverrideRenderStyle, oStyle)
End Sub


Sub export()

    Dim Inv As Inventor.Application
    
    Dim oDoc As Inventor.Document
    Dim oDef As Inventor.ComponentDefinition
    
    Set Inv = GetObject(, "Inventor.Application")
    

    Dim oPart As PartDocument
    Set oPart = Inv.ActiveDocument
    Dim oCompDef As ComponentDefinition
    Set oDef = oPart.ComponentDefinition

    Set oDef = oPart.ComponentDefinition
    
    Dim oFactory As iPartFactory
    Set oFactory = oPart.ComponentDefinition.iPartFactory

    ' disable all bodies
    Dim oSB As SurfaceBody

        ' prepare IGS
    ' Get the IGES translator Add-In.
    Dim oIGESTranslator As TranslatorAddIn
    Set oIGESTranslator = Inv.ApplicationAddIns.ItemById("{90AF7F44-0C01-11D5-8E83-0010B541CD80}")
    Set oSTEPTranslator = Inv.ApplicationAddIns.ItemById("{90AF7F40-0C01-11D5-8E83-0010B541CD80}")
    
    
    Dim oContext As TranslationContext
    Set oContext = Inv.TransientObjects.CreateTranslationContext
    Dim oOptions As NameValueMap
    Set oOptions = Inv.TransientObjects.CreateNameValueMap
    If oIGESTranslator.HasSaveCopyAsOptions(Inv.ActiveDocument, oContext, oOptions) Then
       ' Set geometry type for wireframe.
       ' 0 = Surfaces, 1 = Solids, 2 = Wireframe
       oOptions.value("GeometryType") = 1
       ' To set other translator values:
       ' oOptions.Value("SolidFaceType") = n
       ' 0 = NURBS, 1 = Analytic
       ' oOptions.Value("SurfaceType") = n
       ' 0 = 143(Bounded), 1 = 144(Trimmed)
        oContext.Type = IOMechanismEnum.kFileBrowseIOMechanism
        Dim oData As DataMedium
        Set oData = Inv.TransientObjects.CreateDataMedium
    End If
    
    Dim noFilesBuilded As Integer
    
    'enable one by one and save
    For i = 1 To oDef.SurfaceBodies.Count
        ' hide all
        For Each oSB In oDef.SurfaceBodies
            oSB.Visible = False
        Next
        j = 1
        For Each oSB In oDef.SurfaceBodies
            If j = i Then
                oSB.Visible = True
                oData.FileName = "c:\temp\" & oFactory.DefaultRow.MemberName & "-cev" & j & ".igs"
                oIGESTranslator.SaveCopyAs Inv.ActiveDocument, oContext, oOptions, oData
                
                oData.FileName = "c:\temp\" & oFactory.DefaultRow.MemberName & "-cev" & j & ".stp"
                oSTEPTranslator.SaveCopyAs Inv.ActiveDocument, oContext, oOptions, oData
                noFilesBuilded = noFilesBuilded + 1
            End If
            j = j + 1
        Next
        
    Next i
    
    For Each oSB In oDef.SurfaceBodies
        oSB.Visible = True
    Next
    
    MsgBox noFilesBuilded & " files builded."

End Sub


Sub Sketch2Face2()
    Dim oApp As Inventor.Application
    Set oApp = GetObject(, "Inventor.Application")
    Dim oPart As PartDocument
    Set oPart = oApp.ActiveDocument
    Dim oCompDef As ComponentDefinition
    Set oCompDef = oPart.ComponentDefinition
    Dim oFaces As faces
    Dim oFace As Face
    Dim oEdge As Edge
    Dim x1 As Double
    Dim y1 As Double
    Dim z1 As Double
    Dim x2 As Double
    Dim y2 As Double
    Dim z2 As Double
    Dim cx As Double
    Dim cy As Double
    Dim cz As Double
    
    Dim p As Point
    Dim l As Long
    
    Dim pline As Polyline3d
    
    Dim points() As Double
    Dim edgeNo As Integer
    Dim vertexCoordinates() As Double
    
    Dim vertexIndices() As Long
    
    Open "c:\git\PipeCutter\data.csv" For Output As #1
    
    Dim objUOM As UnitsOfMeasure
    Dim oCOM As Inventor.Point
    
    Dim oDoc As Inventor.Document
    Dim oWS As WorkSurface
    
    Set objUOM = oPart.UnitsOfMeasure
    Set oCOM = oCompDef.MassProperties.CenterOfMass
    
    maxX = oCompDef.RangeBox.MaxPoint.x
    maxY = oCompDef.RangeBox.MaxPoint.y
    maxZ = oCompDef.RangeBox.MaxPoint.z
    
    minX = oCompDef.RangeBox.MinPoint.x
    minY = oCompDef.RangeBox.MinPoint.y
    minZ = oCompDef.RangeBox.MinPoint.z

    centerX = (maxX + minX) / 2
    centerY = (maxY + minY) / 2
    centerZ = (maxZ + minZ) / 2
    
    cogx = objUOM.GetStringFromValue(oCOM.x, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogy = objUOM.GetStringFromValue(oCOM.y, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogz = objUOM.GetStringFromValue(oCOM.z, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    
    'This can be changed to .SideFaces, .EndFaces, or .StartFaces
    'But .Faces will let you choose between all faces
    Set oFaces = oCompDef.SurfaceBodies.Item(1).faces
    edgeNo = 0
    Set oFaces = oCompDef.SurfaceBodies.Item(1).faces
    
    surfaceNo = 0
    pointNo = 0
    
    'oAssyDoc = ThisApplication.ActiveDocument
    Set oDoc = ThisApplication.ActiveDocument
    
    
    'For Each oDoc In oAssyDoc.AllReferencedDocuments
        'set surface bodies visibility
        For Each oFace In oDoc.ComponentDefinition.SurfaceBodies.Item(1).faces

            surfaceNo = surfaceNo + 1
            For Each oEdge In oFace.edges
                
                'Debug.Print oEdge.GeometryForm & " " & oEdge.GeometryType
                
                If oEdge.GeometryType = kBSplineCurve Then
                    Debug.Print "kLineSegmentCurve"
                ElseIf oEdge.GeometryType = kCircleCurve Then
                    'Debug.Print "kCircleCurve"
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                
                Else
                    'Debug.Print oEdge.GeometryType
                End If
                
                
                Debug.Print oEdge.Type
                
                If edgeNo = 199 Or edgeNo = 200 Then
                    Debug.Print oEdge.TangentiallyConnectedEdges.Count
                    Debug.Print oEdge.Parent.AppearanceSourceType
                    For K = 1 To oEdge.EdgeUses.Count
                        Debug.Print oEdge.EdgeUses.Item(K).GeometryForm
                    Next K
                    Debug.Print oEdge.TransientKey
                    
                    
': AppearanceSourceType: kPartAppearance: AppearanceSourceTypeEnum: Module1.Sketch2Face2
                End If
                Debug.Print oEdge.TransientKey
                
                edgeNo = edgeNo + 1
                
                Set pline = ThisApplication.TransientGeometry.CreatePolyline3dFromCurve(oEdge.Geometry, 0.005)
                pointsStr = ""
                For l = 1 To pline.PointCount
                  pointNo = pointNo + 1
                  Set p = pline.PointAtIndex(l)
                  pointsStr = pointsStr & (p.x - centerX) & ";" & (p.y - centerY) & ";" & (p.z - centerZ) & ";"
                Next l
                pointsStr = Left(pointsStr, Len(pointsStr) - 1)
                Print #1, "POLY_" & Trim(Str(surfaceNo)) & "_" & Trim(Str(edgeNo)) & "_" & pointNo & ";" & pointsStr
            
            Next
        Next
    'Next
    Close #1
    
End Sub


' Utility function just to check if the collection
' we are using already includes a given object
Function IsInCollection( _
o As Object, coll As ObjectCollection) As Boolean
  Dim o2 As Object
  For Each o2 In coll
    If o2 Is o Then
      IsInCollection = True
      Exit Function
    End If
  Next
  
  IsInCollection = False
End Function


Public Sub importinventorbom()
    Dim oApp As Inventor.Application
    Set oApp = GetObject(, "Inventor.Application")
    Dim key As String
   
    Dim oDoc As Inventor.AssemblyDocument
    Dim oBOM As Inventor.bom

    Set oDoc = oApp.ActiveDocument
    
    Set oBOM = oDoc.ComponentDefinition.bom
    
    
    ' Set whether first level only or all levels.
    If oBOM.StructuredViewFirstLevelOnly Then
      oBOM.StructuredViewFirstLevelOnly = False
    End If
    
    ' Make sure that the structured view is enabled.
    oBOM.StructuredViewEnabled = True
    
    'Set a reference to the "Structured" BOMView
    Dim oBOMView As BOMView
    Set oBOMView = oBOM.BOMViews.Item(1)
    Set parts = New Dictionary
    parts.CompareMode = TextCompare
    Call QueryBOMRowProperties(oBOMView.BOMRows, 0)
   
    For i = 0 To parts.Count - 1
      key = parts.Keys(i)
      Debug.Print key; parts.Items(i)
    Next i
   
    Set BOMRow = Nothing
    Set oBOM = Nothing
    Set oDoc = Nothing
   Set oApp = Nothing
   
End Sub
Private Sub QueryBOMRowProperties(oBOMRows As BOMRowsEnumerator, level As Integer)
    'Iterate through the contents of the BOM Rows.
    Dim i As Long
    Dim ChildRows As Inventor.BOMRowsEnumerator
    Dim definition As Inventor.AssemblyComponentDefinition
    Dim partName As String, description As String
    For i = 1 To oBOMRows.Count
        ' Get the current row.
        Dim oRow As BOMRow
        Set oRow = oBOMRows.Item(i)
        'Set a reference to the primary ComponentDefinition of the row
        Dim oCompDef As ComponentDefinition
        Set oCompDef = oRow.ComponentDefinitions.Item(1)
        If oCompDef.Type = kPartComponentDefinitionObject Then
            'Get the file property that contains the "Part Number"
            'The file property is obtained from the virtual component definition
            partName = oCompDef.Document.DisplayName
            'description = oCompDef.Document.description
            addPart partName, parts
            ActiveCell.value = Space(level) & partName
            ActiveCell.Offset(RowOffset:=0, ColumnOffset:=1).Activate
            'Get the file property that contains the "Description"
            ActiveCell.value = description
            ActiveCell.Offset(RowOffset:=0, ColumnOffset:=1).Activate
            'Get the quantity
            ActiveCell.value = oRow.ItemQuantity
            ActiveCell.Offset(RowOffset:=1, ColumnOffset:=-2).Activate
        Else
            partName = oCompDef.Document.DisplayName 'oCompDef.Document.PropertySets.Item("Design Tracking Properties").Item("Part Number").value
            'description = oCompDef.Document.description

            'addPart partName, parts
            'Get the file property that contains the "Part Number"
            'The file property is obtained from the virtual component definition
            ActiveCell.value = Space(level) & partName
            ActiveCell.Offset(RowOffset:=0, ColumnOffset:=1).Activate
           
            'Get the file property that contains the "Description"
            'ActiveCell.value = description
            ActiveCell.Offset(RowOffset:=0, ColumnOffset:=1).Activate
            'Get the quantity
            ActiveCell.value = oRow.ItemQuantity
            ActiveCell.Offset(RowOffset:=1, ColumnOffset:=-2).Activate
           
            'Recursively iterate child rows if present.
            If oRow.ComponentDefinitions.Item(1).Type = kAssemblyComponentDefinitionObject Then 'its assembly
              Set definition = oRow.ComponentDefinitions.Item(1)
              If definition.BOMStructure = kNormalBOMStructure Then
                Set ChildRows = definition.bom.BOMViews.Item(1).BOMRows
                If Not ChildRows Is Nothing Then
                    Call QueryBOMRowProperties(ChildRows, level + 1)
                End If
              End If
            End If
        End If
    Next
End Sub

Sub addPart(partName As String, parts As Dictionary)
  If partName = "LetevFiksna" Then
    Debug.Print partName
  End If
  
  Dim value As Integer
  If parts.Exists(partName) Then
    value = parts.Item(partName)
    parts.Remove partName
    parts.Add partName, value + 1
  Else
    parts.Add partName, 1
  End If
End Sub



Sub ExportAcadAndPdf()
    
    Set fs = CreateObject("Scripting.FileSystemObject")
    
    Dim nDirs As Long, nFiles As Long, lSize As Currency
    Dim sDir As String, sSrchString As String, dirPdf As String, dirAcad As String
    Dim dxfFileName As String
    Dim comment As String
    Dim fileNum As Integer
    Dim myDir As String
    Dim setComment As String
    Dim length As Single
    
    setComment = InputBox("Vnesi nacin izdelave:", "Izdelava", "cevnilaser")
    
    
    Debug.Print "Za export izbrana izdelava: " & setComment
    
    Dim dwg As String, dwgFileName As String, pdfFileName As String, fullInventorDrawingFileName As String, xtFileName As String
    Dim dirAss As String
    Dim sep As String
    
    Set dirs = New Dictionary
    dirs.CompareMode = TextCompare
    
    sep = vbTab
    
    Set dirs = New Dictionary
    
    Dim oApp As Inventor.Application
    Set oApp = GetObject(, "Inventor.Application")
    
    Dim ad As AssemblyDocument
    Set ad = oApp.ActiveDocument

    Dim acd As AssemblyComponentDefinition
    Set acd = ad.ComponentDefinition
    
    dirAss = GetDirectory(acd.Document.FullFileName)
    Open dirAss & "\bom.txt" For Output As #1

    dirPdf = dirAss & "pdf\"
    dirAcad = dirAss & "acad\"
    
    Dim massProps As MassProperties
    Dim partDoc As PartDocument
    Dim mass As Variant

    Dim bom As bom
    Set bom = acd.bom
    
    Dim doc As Inventor.Document
    
    On Error Resume Next
    If bom.RequiresUpdate Then
      bom.Update
    End If
    Debug.Print Err.description
    On Error GoTo 0
    
    ' Depending on what you need
    bom.StructuredViewEnabled = True
    'bom.StructuredViewFirstLevelOnly = False
    
    ' Structured BOM view is second if it's enabled
    Dim bv As BOMView
    Set bv = bom.BOMViews("Parts Only")
    
    ' Header
    Print #1, "Izdelava" & sep & "Naziv kosa" & sep & "XT name" & sep & "kolièina" & sep & "masa" & sep & "dolžina" & sep & "risba obstaja"
    
    Dim br As BOMRow
    For Each br In bv.BOMRows
      FullFileName = br.ComponentDefinitions.Item(1).Document.FullFileName
      comment = br.ComponentDefinitions.Item(1).Document.PropertySets.Item("{F29F85E0-4FF9-1068-AB91-08002B27B3D9}").ItemByPropId(kCommentsSummaryInformation).value
      stockNum = br.ComponentDefinitions.Item(1).Document.PropertySets.Item("Design Tracking Properties").Item("Stock Number").value
      
      
      'length = br.ComponentDefinitions.Item(1).Document.PropertySets.Item("Design Tracking Properties").Item("Length").value
      
      'qty = br.TotalQuantity
      'unitQty = br.ItemQuantity
      'pieces = qty / unitQty
      
      
      mass = br.ComponentDefinitions.Item(1).Document.PropertySets.Item("Design Tracking Properties").Item("Mass").value
      
      
      nazivkosa = GetFileName(FullFileName)
      myDir = GetDirectory(FullFileName)
      dwg = myDir & nazivkosa + ".dwg"
      idw = myDir + nazivkosa + ".idw"
      
      DoEvents
      mass = 0
      
      ustreza = False
      If setComment <> "" Then
        If comment = setComment Then
          ustreza = True
        End If
      Else
        If comment <> "" Then
          ustreza = True
        End If
      End If
      Debug.Print comment & " " & nazivkosa
      
      If (br.BOMStructure = kNormalBOMStructure Or br.BOMStructure = kInseparableBOMStructure) And ustreza Then
        If comment = "cevnilaser" Then
            fileNum = fileNum + 1
            exportedFileName = "Si_14687_" & fileNum & "_" & stockNum
        ElseIf comment = "laser" Then
            exportedFileName = nazivkosa
        Else
            exportedFileName = nazivkosa
        End If
        recreateFolderFirstTime dirAcad & comment
        recreateFolderFirstTime dirPdf & comment
      
        dxfFileName = dirAcad & comment & "\" & exportedFileName & ".dxf"
        pdfFileName = dirPdf & comment & "\" & exportedFileName & ".pdf"
        xtFileName = dirAcad & comment & "\" & exportedFileName & ".x_b"
        
        fullInventorDrawingFileName = FindFile(dirAss, nazivkosa & ".dwg", nDirs, nFiles)
        
          'DWGOutUsingTranslatorAddIn oApp, dwgFileName
          If comment = "cevnilaser" Then
            
            If fs.FileExists(fullInventorDrawingFileName) = True Then
              cevnilaser_Num = cevnilaser_Num + 1
              Set doc = oApp.Documents.Open(fullInventorDrawingFileName)
              PublishPDF oApp, pdfFileName
              doc.Close True
            Else
              Print #1, nazivkosa & ", " & br.ItemQuantity & "," & "DWG NE OBSTAJA!"
            End If
            Set doc = oApp.Documents.Open(FullFileName)
            Set partDoc = doc
            If Not partDoc.ComponentDefinition.MassProperties Is Nothing Then
              Set massProps = partDoc.ComponentDefinition.MassProperties
              mass = massProps.mass
            End If
            PublishParasolid oApp, xtFileName
            doc.Close True
          ElseIf comment = "laser" Then
            laser_Num = laser_Num + 1
            If fs.FileExists(fullInventorDrawingFileName) = True Then
              Set doc = oApp.Documents.Open(fullInventorDrawingFileName)
              PublishDXF oApp, dxfFileName
              PublishPDF oApp, pdfFileName
              doc.Close True
            Else
              Print #1, nazivkosa & ", " & br.ItemQuantity & "," & "DWG NE OBSTAJA!"
            End If
          ElseIf comment = "grosmetal" Then
            If fs.FileExists(fullInventorDrawingFileName) = True Then
              Set doc = oApp.Documents.Open(fullInventorDrawingFileName)
              PublishDXF oApp, dxfFileName
              PublishPDF oApp, pdfFileName
              doc.Close True
            Else
              Print #1, nazivkosa & ", " & br.ItemQuantity & "," & "DWG NE OBSTAJA!"
            End If
          ElseIf comment = "struznica" Then
            If fs.FileExists(fullInventorDrawingFileName) = True Then
              Set doc = oApp.Documents.Open(fullInventorDrawingFileName)
              PublishPDF oApp, pdfFileName
              doc.Close True
            Else
              Print #1, nazivkosa & ", " & br.ItemQuantity & "," & "DWG NE OBSTAJA!"
            End If
          End If
          Print #1, comment & sep & nazivkosa & sep & exportedFileName & sep & br.ItemQuantity & sep & mass & sep & length & sep & fs.FileExists(fullInventorDrawingFileName)
      End If
    Next
    Close #1
    Close #2
    Close #3
End Sub

Function GetDirectory(path) As String
   GetDirectory = Left(path, InStrRev(path, "\"))
End Function

Function GetFileName(path) As String
  Dim dir As String
   dir = Left(path, InStrRev(path, "\"))
   ext = Left(path, InStrRev(path, "."))
   
   GetFileName = Mid(path, Len(dir) + 1, Len(ext) - Len(dir) - 1)
End Function

Sub PublishDXF(oApp As Inventor.Application, FileName As String)
    
    Dim oDoc As DrawingDocument
    Set oDoc = oApp.ActiveDocument

    Dim bSaveAsCopyOptions As Boolean
    Dim oAppAddIns As ApplicationAddIns
    Dim oDataMedium As DataMedium
    Dim oDXFTransl As TranslatorAddIn
    Dim oTransObjs As TransientObjects
    Dim oTranslCntxt As TranslationContext
    Dim oNameValMap As NameValueMap
    Dim intIndex As Integer

    Set oAppAddIns = oApp.ApplicationAddIns

    For intIndex = 1 To oAppAddIns.Count
        If InStr(oAppAddIns(intIndex).ShortDisplayName, "Translator: DXF") Then
            Set oDXFTransl = oAppAddIns.Item(intIndex)
            Exit For
        End If
    Next intIndex

    'Translation Objekte setzen
    Set oTransObjs = oApp.TransientObjects
    Set oNameValMap = oTransObjs.CreateNameValueMap
    Set oTranslCntxt = oTransObjs.CreateTranslationContext
    Set oDataMedium = oTransObjs.CreateDataMedium
  
    oTranslCntxt.Type = kFileBrowseIOMechanism

    'bSaveAsCopyOptions = oDXFTransl.HasSaveCopyAsOptions(oDataMedium, oTranslCntxt, oNameValMap)

    oDataMedium.FileName = FileName
    'If oDXFTransl.HasSaveCopyAsOptions(oDataMedium, oTranslCntxt, oNameValMap) Then
        
        Dim strIniFile As String
        strIniFile = "C:\temp\testDXF.ini"

        ' Create the name-value that specifies the ini file to use.
        oNameValMap.value("Export_Acad_IniFile") = strIniFile
    'End If

    Call oDXFTransl.SaveCopyAs(oDoc, oTranslCntxt, oNameValMap, oDataMedium)

' Variablen leeren
    Set oAppAddIns = Nothing
    Set oDataMedium = Nothing
    Set oDXFTransl = Nothing
    Set oTransObjs = Nothing
    Set oTranslCntxt = Nothing
    Set oNameValMap = Nothing

End Sub

Public Sub DWGOutUsingTranslatorAddIn(oApp As Inventor.Application, FileName As String)
   ' Set a reference to the DWG translator add-in.
    Dim oDWGAddIn As TranslatorAddIn
    Dim i As Long
    For i = 1 To oApp.ApplicationAddIns.Count
    If oApp.ApplicationAddIns.Item(i). _
    ClassIdString = _
    "{C24E3AC2-122E-11D5-8E91-0010B541CD80}" Then
        Set oDWGAddIn = oApp. _
                  ApplicationAddIns.Item(i)
        Exit For
    End If
    Next
   
    If oDWGAddIn Is Nothing Then
        MsgBox "The DWG add-in could not be found."
        Exit Sub
    End If
   
   ' Check to make sure the add-in is activated.
    If Not oDWGAddIn.Activated Then
        oDWGAddIn.Activate
    End If
   
   ' Create a name-value map to supply information
    ' to the translator.
    Dim oNameValueMap As NameValueMap
    Set oNameValueMap = oApp. _
       TransientObjects.CreateNameValueMap
   
    Dim strIniFile As String
    strIniFile = "C:\temp\DWGOut.ini"
   
    ' Create the name-value that specifies
    ' the ini file to use.
    Call oNameValueMap.Add _
         ("Export_Acad_IniFile", strIniFile)
   
   ' Create a translation context and define
    ' that we want to output to a file.
    Dim oContext As TranslationContext
    Set oContext = oApp.TransientObjects. _
                              CreateTranslationContext
    oContext.Type = kFileBrowseIOMechanism
   
    
   ' Define the type of output by
    ' specifying the filename.
    Dim oOutputFile As DataMedium
    Set oOutputFile = oApp. _
       TransientObjects.CreateDataMedium
    oOutputFile.FileName = FileName
   
  
   
   ' Call the SaveCopyAs method of the add-in.
    Call oDWGAddIn.SaveCopyAs _
               (oApp.ActiveDocument, _
                                      oContext, _
                                  oNameValueMap, _
                                     oOutputFile)
End Sub

Private Function FindFile(ByVal sFol As String, sFile As String, _
   nDirs As Long, nFiles As Long) As String
   Dim tFld As Folder, tFil As File, FileName As String
   Set FSO = CreateObject("Scripting.FileSystemObject")
   On Error GoTo Catch
   Set fld = FSO.GetFolder(sFol)
   FileName = dir(FSO.BuildPath(fld.path, sFile), vbNormal Or _
                  vbHidden Or vbSystem Or vbReadOnly)
   While Len(FileName) <> 0
      FindFile = FindFile + FSO.BuildPath(fld.path, FileName)
      nFiles = nFiles + 1
      FindFile = FSO.BuildPath(fld.path, FileName)  ' Load ListBox
      If FindFile <> "" Then
       Exit Function
      End If
      FileName = dir()  ' Get next file
   Wend
   nDirs = nDirs + 1
   If fld.SubFolders.Count > 0 Then
      For Each tFld In fld.SubFolders
         FindFile = FindFile + FindFile(tFld.path, sFile, nDirs, nFiles)
         If FindFile <> "" Then
          Exit Function
         End If
      Next
   End If
   Exit Function
Catch:  FileName = ""
       Resume Next
End Function

Public Sub PublishPDF(oApp As Inventor.Application, FileName As String)
    ' Get the PDF translator Add-In.
    Dim PDFAddIn As TranslatorAddIn
    Set PDFAddIn = oApp.ApplicationAddIns.ItemById("{0AC6FD96-2F4D-42CE-8BE0-8AEA580399E4}")

    'Set a reference to the active document (the document to be published).
    Dim oDocument As Document
    Set oDocument = oApp.ActiveDocument

    Dim oContext As TranslationContext
    Set oContext = oApp.TransientObjects.CreateTranslationContext
    oContext.Type = kFileBrowseIOMechanism

    ' Create a NameValueMap object
    Dim oOptions As NameValueMap
    Set oOptions = oApp.TransientObjects.CreateNameValueMap

    ' Create a DataMedium object
    Dim oDataMedium As DataMedium
    Set oDataMedium = oApp.TransientObjects.CreateDataMedium

    ' Check whether the translator has 'SaveCopyAs' options
    If PDFAddIn.HasSaveCopyAsOptions(oDocument, oContext, oOptions) Then

        ' Options for drawings...

        oOptions.value("All_Color_AS_Black") = 0

        'oOptions.Value("Remove_Line_Weights") = 0
        oOptions.value("Vector_Resolution") = 1600
        'oOptions.Value("Sheet_Range") = kPrintAllSheets
        'oOptions.Value("Custom_Begin_Sheet") = 2
        'oOptions.Value("Custom_End_Sheet") = 4

    End If

    'Set the destination file name
    oDataMedium.FileName = FileName

    'Publish document.
    Call PDFAddIn.SaveCopyAs(oDocument, oContext, oOptions, oDataMedium)
End Sub


Public Sub PublishIGS(oApp As Inventor.Application, FileName As String)
  ' Translator: Parasolid Text {8F9D3571-3CB8-42F7-8AFF-2DB2779C8465}
  'ESKD Support {005B21FC-8537-4926-9F57-3A3216C294C3}
  'Translator: DWF {0AC6FD95-2F4D-42CE-8BE0-8AEA580399E4}
  'Translator: PDF {0AC6FD96-2F4D-42CE-8BE0-8AEA580399E4}
  'Translator: DWFx {0AC6FD97-2F4D-42CE-8BE0-8AEA580399E4}
  'Assembly Bonus Tools {0BB5AE99-15A3-4B00-9731-210ED5A4E7B2}
  'Translator: JT {16625A0E-F58C-4488-A969-E7EC4F99CACD}
  'Autodesk i-drop Translator {21DB88B0-BFBF-11D4-8DE6-0010B541CAA8}
  'Simulation: Dynamic Simulation {24307C2D-2E7F-486F-94A0-0B45E11CB3F6}
  'Translator: CATIA V5 Part Export {2FEE4AE5-36D3-4392-89C7-58A9CD14D305}
  'InventorAddIn {3032AF63-8907-406A-BFCC-8E1459F9A927}
  'Content Center {3D88D7B5-6DD8-4205-A2B5-2B51F7BF74A7}
  'Translator: SolidWorks {402BE503-725D-41CB-B746-D557AB83BAF1}
  'Simulation: Stress Analysis {432334FF-1827-4649-BFC6-E4D1C5167E78}
  'Translator: Pro/ENGINEER {46D96B7A-CF8A-49C9-8703-2F40CFBDF547}
  'DrawingTools {48A74AAC-E196-4AF0-9EC2-28C7C5150645}
  'Inventor Vault {48B682BC-42E6-4953-84C5-3D253B52E77B}
  'Routed Systems: Tube & Pipe {4D39D5F1-0985-4783-AA5A-FC16C288418C}
  'Simulation: ANSYS Workbench {4E495F46-CE6D-45B4-BF41-F0C3C2E12039}
  'Translator: STL {533E9A98-FC3B-11D4-8E7E-0010B541CD80}
  'Autodesk DWF Markup Manager {55EBD0FA-EF60-4028-A350-502CA148B499}
  'Translator: Pro/ENGINEER Granite {66CB2667-73AD-401C-A531-64EC701825A1}
  'Autodesk IDF Translator {6C5BBC04-5D6F-4353-94B1-060CD6554444}
  'AEC Exchange {842004D5-C360-43A8-A00D-D7EB72DAAB69}
  'Translator: SAT {89162634-02B6-11D5-8E80-0010B541CD80}
  'Translator: CATIA V5 Product Export {8A88FC01-0C32-4B3E-BE12-DDC8DF6FFF18}
  'Translator: Pro/ENGINEER Neutral {8CEC09E3-D638-4E8F-A6E1-0D1E1A5FC8E3}
  'Translator: CATIA V5 Import {8D1717FA-EB24-473C-8B0F-0F810C4FC5A8}
  'Translator: Parasolid Text {8F9D3571-3CB8-42F7-8AFF-2DB2779C8465}
  'Translator: STEP {90AF7F40-0C01-11D5-8E83-0010B541CD80}
  'Translator: IGES {90AF7F44-0C01-11D5-8E83-0010B541CD80}
  'Translator: UGS NX {93D506C4-8355-4E28-9C4E-C2B5F1EDC6AE}
  'GeneralTools {95413AE5-1C63-4373-A614-2649F5BF8AA6}
  'Content Center Item Translator {A547F528-D239-475F-8FC6-8F97C4DB6746}
  'Translator: Parasolid Binary {A8F8F8E5-BBAB-4F74-8B1B-AC011251F8AC}
  'Frame Generator {AC211AE0-A7A5-4589-916D-81C529DA6D17}
  'Simulation: Stress Analysis {B3D04494-EDD2-4FDC-9EC2-30BAF8D6B77B}
  'CustomUI Sample {B6CD8174-8817-4AF2-9561-C0F273ABF5D8}
  'Drag & Drop Interoperability {B95D705C-E915-4A5B-A498-E73AC98923A2}
  'Design Accelerator {BB8FE430-83BF-418D-8DF9-9B323D3DB9B9}
  'Translator: DWG {C24E3AC2-122E-11D5-8E91-0010B541CD80}
  'Translator: DXF {C24E3AC4-122E-11D5-8E91-0010B541CD80}
  'Routed Systems: Cable & Harness {C6107C9D-C53F-4323-8768-F65F857F9F5A}
  'Translator: Alias {DC5CD10A-F6D1-4CA3-A6E3-42A6D646B03E}
  'AutoLimits {F330512C-8773-4A60-A0D3-E3CDA0B9ED0D}
  'Inventor Studio {F3D38928-74D1-4814-8C24-A74CE8F3B2E3}
  'EDM Addin {FFFFFFFF-87FB-40FB-BC8E-F397C40B59F1}
  ' Get the IGES translator Add-In.

    Dim oIGESTranslator As TranslatorAddIn
    Set oIGESTranslator = oApp.ApplicationAddIns.ItemById("{90AF7F44-0C01-11D5-8E83-0010B541CD80}")

    If oIGESTranslator Is Nothing Then
        MsgBox "Could not access IGES translator."
        Exit Sub
    End If

    Dim oContext As TranslationContext
    Set oContext = oApp.TransientObjects.CreateTranslationContext
    Dim oOptions As NameValueMap
    Set oOptions = oApp.TransientObjects.CreateNameValueMap
    If oIGESTranslator.HasSaveCopyAsOptions(oApp.ActiveDocument, oContext, oOptions) Then
        ' Set geometry type for wireframe.
        ' 0 = Surfaces, 1 = Solids, 2 = Wireframe
        oOptions.value("GeometryType") = 1

        ' To set other translator values:
        ' oOptions.Value("SolidFaceType") = n
        ' 0 = NURBS, 1 = Analytic

        ' oOptions.Value("SurfaceType") = n
        ' 0 = 143(Bounded), 1 = 144(Trimmed)

        oContext.Type = kFileBrowseIOMechanism

        Dim oData As DataMedium
        Set oData = oApp.TransientObjects.CreateDataMedium
        oData.FileName = FileName

        Call oIGESTranslator.SaveCopyAs(oApp.ActiveDocument, oContext, oOptions, oData)
    End If


End Sub


Public Sub PublishParasolid(oApp As Inventor.Application, FileName As String)
    Dim oXTTranslator As TranslatorAddIn
    Dim oDataMedium As Inventor.DataMedium
    Set oXTTranslator = oApp.ApplicationAddIns.ItemById("{8F9D3571-3CB8-42F7-8AFF-2DB2779C8465}")

    Set oDocument = oApp.ActiveDocument
    Set oContext = oApp.TransientObjects.CreateTranslationContext
    oContext.Type = IOMechanismEnum.kFileBrowseIOMechanism
    Set oOptions = oApp.TransientObjects.CreateNameValueMap
    Set oDataMedium = oApp.TransientObjects.CreateDataMedium
    If oXTTranslator.HasSaveCopyAsOptions(oDocument, oContext, oOptions) Then
      oOptions.value("Version") = 24
      oOptions.value("OutputFileType") = 0
      oOptions.value("GeometryType") = 1
      oOptions.value("OutputFileType") = 1
      oOptions.value("ExportUnits") = 5
      oOptions.value("IncludeSketches") = True
    End If
    'Set the destination file name
    oDataMedium.FileName = FileName
    
    'Publish document.
    oXTTranslator.SaveCopyAs oDocument, oContext, oOptions, oDataMedium

End Sub

'Public Function getToken(r As Range, sep As String, index As Integer) As String
'  a = Split(r.Text, sep)
'  getToken = a(index)
'End Function

Sub createDir(pot As String)
  If Len(dir(pot, vbDirectory)) = 0 Then
     MkDir pot
  End If
End Sub


Function DeleteFolders(fldr)
  If dir(fldr, vbDirectory) <> "" Then
    Set FileSys = CreateObject("Scripting.FileSystemObject")
    Set objFolder = FileSys.GetFolder(fldr)
    Dim sf As Folder
    For Each sf In objFolder.SubFolders
      DeleteFolders sf  '<- recurse here
    Next
    Debug.Print "deleted: " & fldr
    objFolder.Delete
    Set FileSys = Nothing
    Set objFolder = Nothing
  End If
End Function
 
 
Sub recreateFolderFirstTime(folderPath As String)
  If Not dirs.Exists(folderPath) Then
    Debug.Print "deleting folder: " & folderPath
    DeleteFolders folderPath
    If dir(folderPath, vbDirectory) = "" Then
    
      Dim wsh As Object
      Set wsh = VBA.CreateObject("WScript.Shell")
      Dim waitOnReturn As Boolean: waitOnReturn = True
      Dim windowStyle As Integer: windowStyle = 1

      cmd = "cmd /c mkdir """ & folderPath & """"
      wsh.Run cmd, windowStyle, waitOnReturn
    
      'Shell ("cmd /c mkdir """ & folderPath & """")
    End If
    dirs.Add folderPath, folderPath
  End If
End Sub

 Sub GetInventorApplicationObject()

        If inventorApp Is Nothing Then
            inventorApp = CreateObject("Inventor.Application")
        Else
            inventorApp = GetObject("Inventor.Application")
        End If


    End Sub

' refereces to incluede:
'                           Microsoft Scripting Framework
'                           Autodesk Inventor Object Library


Sub SelectOuterEdgesOfConnectedFaces()
  Dim doc As PartDocument, p As Inventor.Point, prevP As Inventor.Point
  
  Dim ThisApplication As Inventor.Application
  
'
'    If ThisApplication Is Nothing Then
'        Set ThisApplication = CreateObject("Inventor.Application")
'    Else
'    End If
'
  
    Set ThisApplication = GetObject(, "Inventor.Application")
  
  Set doc = ThisApplication.ActiveDocument
  
  
  Dim f As Face, e As Edge
  
  Dim edges As ObjectCollection
    Dim tro As TransientObjects
    Set tro = ThisApplication.TransientObjects
    Set edges = tro.CreateObjectCollection
  
  If doc.SelectSet(1).Type = kFaceObject Then
    Set f = doc.SelectSet(1)
    Call doc.SelectSet.Clear
    
    Dim faces As ObjectCollection
    Set faces = tro.CreateObjectCollection
    
    Call GetAllTangentiallyConnectedFaces(f, faces)
        
    Call GetOuterEdgesOfFaces(faces, edges)
    Call doc.SelectSet.SelectMultiple(edges)
  End If
  
  ' if edges count = 0 (couldnt be calculated from face) lets assume edges are selected
  If edges.Count = 0 Then
    For i = 1 To doc.SelectSet.Count
      Set e = doc.SelectSet.Item(i)
      edges.Add e
    Next i
  End If
    
    Open "c:\git\PipeCutter\data.csv" For Output As #1
    
    Dim objUOM As UnitsOfMeasure
    Dim oCOM As Inventor.Point
    
    Dim oDoc As Inventor.Document
    Dim oWS As WorkSurface
    Dim oPart As Inventor.PartDocument
    Dim oCompDef As ComponentDefinition
    
    
    Set oPart = doc
    Set oCompDef = oPart.ComponentDefinition

    Set objUOM = oPart.UnitsOfMeasure
    Set oCOM = oCompDef.MassProperties.CenterOfMass
    
    maxX = oCompDef.RangeBox.MaxPoint.x
    maxY = oCompDef.RangeBox.MaxPoint.y
    maxZ = oCompDef.RangeBox.MaxPoint.z
    
    minX = oCompDef.RangeBox.MinPoint.x
    minY = oCompDef.RangeBox.MinPoint.y
    minZ = oCompDef.RangeBox.MinPoint.z

    centerX = ((maxX + minX) / 2) * 10
    centerY = ((maxY + minY) / 2) * 10
    centerZ = ((maxZ + minZ) / 2) * 10
    
    cogx = objUOM.GetStringFromValue(oCOM.x, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogy = objUOM.GetStringFromValue(oCOM.y, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogz = objUOM.GetStringFromValue(oCOM.z, UnitsTypeEnum.kDefaultDisplayLengthUnits)
  
  
  For Each e In edges
    Set prevP = Nothing
    Set pline = ThisApplication.TransientGeometry.CreatePolyline3dFromCurve(e.Geometry, 0.001)
    pointsStr = ""
    For l = 1 To pline.PointCount
      pointNo = pointNo + 1
      Set p = pline.PointAtIndex(l)
      If (Not prevP Is Nothing) Then
        length = Math.Sqr((p.x - prevP.x) ^ 2 + (p.y - prevP.y) ^ 2 + (p.z - prevP.z) ^ 2)
        If length > 5 Then
            midX = (p.x + prevP.x) / 2
            midY = (p.y + prevP.y) / 2
            midZ = (p.z + prevP.z) / 2
            pointsStr = pointsStr & (midX * 10 - centerX) & ";" & (midY * 10 - centerY) & ";" & (midZ * 10 - centerZ) & ";"
        End If
      End If
      pointsStr = pointsStr & (p.x * 10 - centerX) & ";" & (p.y * 10 - centerY) & ";" & (p.z * 10 - centerZ) & ";"
      Set prevP = pline.PointAtIndex(l)
    Next l
    pointsStr = Left(pointsStr, Len(pointsStr) - 1)
    Print #1, "POLY_" & Trim(Str(surfaceNo)) & "_" & Trim(Str(edgeNo)) & "_" & pointNo & ";" & pointsStr
  Next
  Close #1
  
  MsgBox ("Ended")
End Sub

' Recursively collect all tangent faces
Sub GetAllTangentiallyConnectedFaces( _
f As Face, faces As ObjectCollection)
  Dim f2 As Face
  For Each f2 In f.TangentiallyConnectedFaces
    If Not IsInCollection(f2, faces) Then
      Call faces.Add(f2)
      Call GetAllTangentiallyConnectedFaces(f2, faces)
    End If
  Next
End Sub

' Check all edges, but ignore common
' edges with other faces
Sub c(faces As ObjectCollection, edges As ObjectCollection)
  Dim f As Face
  For Each f In faces
    Dim e As Edge
    For Each e In f.edges
      Dim f2 As Face
      For Each f2 In e.faces
        If (Not f Is f2) And _
           (Not IsInCollection(f2, faces)) And _
           (Not IsInCollection(e, edges)) Then
          Call edges.Add(e)
        End If
      Next
    Next
  Next
End Sub
 
' Check all edges, but ignore common
' edges with other faces
Sub GetOuterEdgesOfFaces(faces As ObjectCollection, edges As ObjectCollection)
  Dim f As Face
  For Each f In faces
    Dim e As Edge
    For Each e In f.edges
      Dim f2 As Face
      For Each f2 In e.faces
        If (Not f Is f2) And _
           (Not IsInCollection(f2, faces)) And _
           (Not IsInCollection(e, edges)) Then
          Call edges.Add(e)
        End If
      Next
    Next
  Next
End Sub



